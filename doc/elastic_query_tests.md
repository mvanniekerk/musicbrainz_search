### Tests with elasticsearch query DSL 
The following queries seem to be completely equivalent:

```json
{
    "query" : { 
        "query_string" : { 
            "query" : "mozart AND clarinet AND quintet",
            "fields" : ["artists", "composers", "names"]
        }
    } 
}
```
Using the lucene query string query

```json
{
	"query": {
		"bool" : {
			"should" : [
				{
					"multi_match" : {
						"query": "Mozart",
						"fields" : ["artists" , "composers" , "names"]
					}
				},
				{
					"multi_match" : {
						"query": "clarinet",
						"fields" : ["artists" , "composers" , "names"]
					}
				},
				{
					"multi_match" : {
						"query": "quintet",
						"fields" : ["artists" , "composers" , "names"]
					}
				}
			]
		}
	}
}
```
Using only ElasticSearch DSL

```json
{
    "_source" : false,
    "size" : 20,
    "query" : { 
        "query_string" : { 
            "query" : "mozart AND clarinet AND quintet",
            "fields" : ["artists", "composers", "names"]
        }
    },
    "highlight" : { 
        "number_of_fragments" : 0, 
        "fields" : { 
            "artists" : {}, 
            "composers" : {}, 
            "names" : {} 
        } 
    }
}
```

This is another way to get the result set, it is quite bit (8 times) slower, 
but the result is very pretty. Problem is that this method will not return 
all fields, only fields with matches.

```json
{
    "query" : { 
		"bool" : {
			"must" : [
				{ 
					"query_string" : { 
						"query" : "clarinet AND quintet",
						"fields" : ["artists.folded", "composers.folded", "names.folded"]
					} 
				},
				{ 
					"match" : { 
						"composers.folded" : {
							"query" : "mozart",
							"operator" : "or",
							"zero_terms_query" : "all"
						}
					} 
				},
				{ 
					"match" : { 
						"artists.folded" : {
							"query" : "hagen",
							"operator" : "or",
							"zero_terms_query" : "all"
						}
					} 
				}
			]
		}
    }
}
```

This query will also filter on the artists and composers field specifically.

```json
{    
	"settings": {
      "index": {
        "number_of_shards": "5",
        "number_of_replicas": "1"
      },
      "analysis": {
        "analyzer": {
          "folding": {
            "tokenizer": "standard",
            "filter":  [ "lowercase", "asciifolding" ]
          }
        }
      }
    },
	
	"mappings": {
      "work": {
        "properties": {
          "artists": {
            "type": "text",
            "analyzer": "standard",
		    "fields": {
			  "folded": { 
			    "type":       "text",
			    "analyzer":   "folding"
			  }
		    }
          },
          "composers": {
            "type": "text",
            "analyzer": "standard",
		    "fields": {
			  "folded": { 
			    "type":       "text",
			    "analyzer":   "folding"
			  }
		    }
          },
          "names": {
            "type": "text",
            "analyzer": "standard",
		    "fields": {
			  "folded": { 
			    "type":       "text",
			    "analyzer":   "folding"
			  }
		    }
          }
        }
      }
    }
}
```
Note that a keyword field cannot have an analyzer (or at least a custom analyzer).

Since the search of tokens that were spelled as unicode kept giving empty results (Dvorak), I decided to change the 
index settings.

Use the snippet above to instantiate the database. 
 