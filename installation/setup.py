import requests
import sys

url = "http://192.168.99.100:9200/musicbrainz"

def init():
    data = open("elastic_settings.json", "r")
    headers = {'Content-Type' : 'application/json'}
    r = requests.put(url, data = data.read(), headers = headers)

    jr = r.json()

    try:
        err = jr["error"]
        print("Operation failed: " + err["type"])
    except KeyError:
        print("Operation succesfull, response: " + jr)

def remove():
    r = requests.delete(url)
    jr = r.json()
    try:
        err = jr["error"]
        print("Operation failed: " + err["type"])
    except KeyError:
        print("Operation succesfull, response: " + jr)

if __name__ == "__main__":
    remove()
    init()
