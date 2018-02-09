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

Both queries give almost prefect results.

#### TODO: 
- [x] Convert the musicbrainz SQL to searchable elasticsearch documents
- [x] Add the right primary title, primary composer to the elastic documents
- [x] Convert the elm frontend to make use of the new DSL
- [x] Write a new backend class to interface with the elastic server