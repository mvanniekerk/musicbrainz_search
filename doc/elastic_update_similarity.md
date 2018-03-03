```http request
POST http://192.168.99.100:9200/musicbrainz/_close
```

```http request
PUT http://192.168.99.100:9200/musicbrainz/_settings
```

```json
{
    "settings" : {
        "index" : {
            "similarity" : {
              "my_similarity" : {
                "type" : "BM25",
                "k1" : "1.2",
                "b" : "0.75",
                "discount_overlaps" : "true"
                
              }
            }
        }
    }
}
```


```http request
POST http://192.168.99.100:9200/musicbrainz/_open
```