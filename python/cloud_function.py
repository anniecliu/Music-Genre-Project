import math, librosa
from pathlib import Path

import numpy as np
import urllib.request


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

    signal, sample_rate = librosa.load(wav_file_path, sr=SAMPLE_RATE, duration=30)

    samples_per_segment = int(SAMPLES_PER_TRACK / 6)
    num_mfcc_vectors_per_segment = math.ceil(samples_per_segment / 512)

    mfcc = librosa.feature.mfcc(signal, sample_rate, n_mfcc=13, n_fft=2048, hop_length=512)
    mfcc = mfcc.T

    data = np.array(mfcc)

    return data[ ..., np.newaxis ]

def download_file( track_url ):
  print('inside download_file', track_url)
  file_path = r'/tmp/' + track_url.split('/')[-1]
  print('file_path', file_path)
  urllib.request.urlretrieve( track_url, file_path )
  return file_path 

def predict_genre(request):
    print('inside predict_genre')
    print('request', request)
    
    z = ['hiphop', 'reggae', 'metal', 'jazz', 'disco', 'pop', 'classical', 'country', 'blues', 'rock']
    
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

    feature = process_WAV_file( test_file_path )
    print( feature.shape )
    # print( feature )


    # project = 'genre-classifier-293000'
    # model = 'genre_classifier_model'
    # version = 'genre_classifier_model_version'
	
    project = 'genre-classifier-293000'
    model = 'genre_classifier_model_1'
    version = 'genre_classifier_model_version_1'	

    prediction = predict_json2(project, 'us-central1', model, np.array([feature]).tolist(), version)

    print('prediction1', prediction)

    argmaxval=np.argmax(prediction, axis=1)
    print('argmaxval', argmaxval)

    print('predicted genre: ', z[argmaxval[0]])

    return z[argmaxval[0]]
