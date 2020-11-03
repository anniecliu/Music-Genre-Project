import math, librosa
from pathlib import Path

import numpy as np
import urllib.request
import statistics


import googleapiclient.discovery
from google.api_core.client_options import ClientOptions

def predict_json2(project, region, model, instances, version=None):
    # Create the ML Engine service object.
    # To authenticate set the environment variable
    # GOOGLE_APPLICATION_CREDENTIALS=<path_to_service_account_file>
    prefix = "{}-ml".format(region) if region else "ml"
    api_endpoint = "https://{}.googleapis.com".format(prefix)
    client_options = ClientOptions(api_endpoint=api_endpoint)
    service = googleapiclient.discovery.build(
        'ml', 'v1', client_options=client_options)
    name = 'projects/{}/models/{}'.format(project, model)

    if version is not None:
        name += '/versions/{}'.format(version)

    response = service.projects().predict(
        name=name,
        body={'instances': instances}
    ).execute()

    if 'error' in response:
        raise RuntimeError(response['error'])

    print('response', response)
    print('predictions', response['predictions'])

    return response['predictions']


def process_WAV_file( wav_file_path ):
    print('inside process_WAV_file', wav_file_path)

    SAMPLE_RATE = 22050
    TRACK_DURATION = 30 # measured in seconds
    SAMPLES_PER_TRACK = SAMPLE_RATE * TRACK_DURATION
    NUM_SEGMENTS = 6

    n_mfcc=13
    n_fft=2048
    hop_length=512

    signal, sample_rate = librosa.load(wav_file_path, sr=SAMPLE_RATE, duration=30)

    num_samples_per_segment = int(SAMPLES_PER_TRACK / 6)
    num_mfcc_vectors_per_segment = math.ceil(num_samples_per_segment / hop_length)

    # Creates list of mfccs from a single wav file
    list_of_mfccs = []

    for segment in range(6):
        for s in range(6):
            start_sample = num_samples_per_segment * s # s=0 -> 0 seconds
            finish_sample = start_sample + num_samples_per_segment # s=0 -> num_samples_per_segment

            mfcc = librosa.feature.mfcc(signal[start_sample:finish_sample],
                                        sr=sample_rate,
                                        n_fft=n_fft,
                                        n_mfcc=n_mfcc,
                                        hop_length=hop_length)                    

        mfcc = mfcc.T
        data = np.array(mfcc)
        list_of_mfccs.append(data[ ..., np.newaxis])
    return list_of_mfccs
    # return data[ ..., np.newaxis]



def download_file( track_url ):
    print('inside download_file', track_url)
    file_path = r'/tmp/' + track_url.split('/')[-1]
    print('file_path', file_path)
    urllib.request.urlretrieve( track_url, file_path )
    return file_path 

def predict_genre(request):
    print('inside predict_genre')
    print('request', request)
    
    # z = ['hiphop', 'reggae', 'metal', 'jazz', 'disco', 'pop', 'classical', 'country', 'blues', 'rock']
    z = ['jazz', 'reggae', 'pop', 'country', 'rock', 'disco', 'metal', 'hiphop', 'classical', 'blues']
    
    request_json = request.get_json(silent=True)
    request_args = request.args
    
    print('request_json', request_json)
    print('request_args', request_args)
    
    if request_json and 'wav_audio_file_url' in request_json:
        test_url = request_json['wav_audio_file_url']
        print('wav_audio_file_url', test_url)
    elif request_args and 'wav_audio_file_name' in request_args:
        name = request_args['wav_audio_file_name']
        test_url = 'https://storage.googleapis.com/genre-classifier-audio-files-bucket/'+name
        print('args wav_audio_file_name', name)
        print('test_url', test_url)
    elif request_json and 'wav_audio_file_name' in request_json:
        name = request_json['wav_audio_file_name']
        test_url = 'https://storage.googleapis.com/genre-classifier-audio-files-bucket/'+name
        print('wav_audio_file_name', name)
        print('test_url', test_url)
    else:
        test_url = 'https://storage.googleapis.com/genre-classifier-audio-files-bucket/sweet-child-o-mine.wav'
        print('else test_url', test_url)

    test_file_path = download_file(test_url)

    features = process_WAV_file( test_file_path )
    project = 'genre-classifier-293000'
    model = 'genre_classifier_model_5s'
    version = 'genre_classifier_model_5s_version_1'

    # prediction = predict_json2(project, 'us-central1', model, np.array([features]).tolist(), version)


    # features = process_WAV_file( test2)

    # Creating list of mfcc features
    list_of_predicted = []

    for mfcc in range(len(features)):
        print('for mfcc', mfcc)
        print('for features[mfcc]', features[mfcc])
        # pred, pred_in = predict(model, features[mfcc])
        prediction = predict_json2(project, 'us-central1', model, np.array([features[mfcc]]).tolist(), version)
        
        argmaxval=np.argmax(prediction, axis=1)
        print('argmaxval', argmaxval)
        print('predicted genre: ', mfcc, z[argmaxval[0]])
        # list_of_predicted.append(prediction.tolist())
        list_of_predicted.append([argmaxval[0]])

    # Converting to int to get Mode
    list_of_predicted = [int(str(x).strip('[]')) for x in list_of_predicted]
    mode = statistics.mode(list_of_predicted)

    print('list_of_predicted', list_of_predicted)
    print('mode', mode)

    # Predicted Classification
    print('z[mode]', z[mode])

    return z[mode]

