# problem statement
- there are old newspaper articles that reference concerts
- these articles have been digitalized recently
- the piece of music that the article references is not always clear
- the piece of music referenced may be spelled differently at different times
- there may be multiple different names for the same piece of music
- the composer may be known under different names
- there may be mistakes in the transcription of the texts

## requirement
- give a text (possibly containing mistakes) as input
- give a reference to a piece as output

# solution
- domain of NLP
- sounds like the same problem that every search engine needs to solve
- create a search engine
- find a good data source to populate the search engine with

## data acquisition idea
- musicbrainz, an open music encyclopedia
- big, hand curated database with the same collaboration style as wikipedia or open street map
- contains all meta data belonging to a large part of all recorded music
- there are often multiple recordings for each classical work
- every composer has many works, each work has many recordings
- each recording has metadata
- the metadata referencing a work will be different for each recording
- these different descriptions can be combined in a bag of words

### data acquisition
- for each work, we are interested in all aliases that can be found for this work,
and all aliases that can be found for the composer of this  work.

## the search engine
- first approach hand crafted
- result quality was good enough to see the promise of the idea
- but the speed was unbearably slow
- second approach, use of the shelf ElasticSearch.

### optimizations
- weighted zone scoring

# future applications for this solution
- a table based database can be searched very effectively by throwing fields
together in a bag-of-words model
- there is a similar problem with books