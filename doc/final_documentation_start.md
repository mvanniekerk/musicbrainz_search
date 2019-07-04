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
- the musicbrainz database is available via a virtualbox virtual machine, containing 
some docker images. 
- the database runs on PostgreSQL. 

#### database representation of works
- the canonical representation of each work is its musicbrainz id, a uuid.
- the name of the work is also of interest
- the mb db also keeps track of aliases for each work
- and recordings where this work is performed
- recordings have their own aliases
- and can have one or more tracks, which is the mb way of specifying a release of a recording.
- these tracks also have names that are collected
- recordings also always have artists
- these artists can also be extremely relevant to the work, especially in non-classical music
- even in classical music, we may associate some artists with a work.
- so we need to store all the artists in the recording.
- each work also has one or more composers. 
- these composers, including their aliases need to be retrieved as well
- aliases are especially important for composers and works composed outside western europe
- the canonical name of Tchaikovsky's Swan Lake is Лебединое озеро in the musicbrainz database.

#### Classical works often have a hierarchical structure
- every work can also have related works. Works can be made up of multiple parts: 
an orchestral suite can have movements.
- for example, bach cello suites -> first cello suite -> prelude
- the search engine needs to reflect this hierarchical nature.
- for each work the broader work it is a part of is stored, if it exists.
- when displaying results, the results are shown hierarchically
- when scoring the performance of the search engine, the first leaf of the tree has the first
position, but also the direct ancestors of this work in the search result tree will have the first position.
- This scoring method was chosen because it is not always clear to which part of the tree a search query
refers.

#### The retrieval process
- The complete musicbrainz database virtual machine is about 100 GB.
- The computer this project was written on contains 8 GB of ram.
- Therefore the queries needed to retrieve the necessary information from the database needed to be windowed
quite aggressively.
- At most, the metadata information of 5000 works is retrieved.
- This keeps the java runtime under about 2 GB of ram
- The musicbrainz database is also quite aggressively normalized.
- Some of the queries described in the previous section require 5 joins.
- There are just about 900000 works in the musicbrainz database
- When all data is combined, some works have a couple of KB of metadata that needs to be retrieved
- All these considerations together mean that the metadata aggregation process is quite slow
- One full metadata aggregation run takes about 6 hours. 

## the search engine
- first approach hand crafted
- result quality was good enough to see the promise of the idea
- but the speed was unbearably slow
- second approach, use of the shelf ElasticSearch.
- ES is extremely fast, and almost as configurable as a hand crafted solution
- For each work, three lists are stored
- A list of all artists that performed the work
- A list of all composers + all aliases of these composers
- A list of all the names and titles that are associated with the work
- ES can throw all words in these lists together to search through them as a bag of words for each work
- The lists are stored separately for presentation and optimization reasons

### testing performance of the search engine
- about 50 hand made test cases
- a typical search query, and the canonical musicbrainz uuid
- during the development, there was no access to the newspaper sources that the project is about

- another test source is available, the last fm million song dataset
- in this dataset there are 960000 track names, together with a musicbrainz uuid
- with the musicbrainz uuid, it is possible to get a "search string", by combining the track name
and the composer found for the uuid in the musicbrainz database.
- the two are appended together with a space in between
- there are only 220000 tracks that have a corresponding "work"
- of those tracks, about 10000 are likely to be classical
- for each test run, 200 random last fm "search strings" are taken
- note that the original producer of this dataset had to go through a similar process as I did
- so the muscbrainz string match may not be 100% reliable

### optimizations
- ES gives the option to use custom similarity scoring modules
- The similarity scoring determines the search results
- The default is (BM25)[https://en.wikipedia.org/wiki/Okapi_BM25], which is a variant of TF-IDF.
- BM25 takes two parameters that determine the behaviour of the algorithm.
- Finding the optimal parameters is a question of trial and error

- each work consists of three lists, artists, composers and work names.
- the list of artists may not be as important as the list of work names.
- ES can give a weight to each list.
- weighted zone scoring

- there are multiple methods available for combining the fields
- search for the query in every list, and then add the resulting scores
- ES calls this a most fields query
- symphony is a common term in the name of a piece, so it will have a low IDF.
But if an artist may have symphony in their name, it will be rare, so it will have a very high
IDF. This may put this search result higher than it should
- other method: combine the terms from all lists together, but keep the weights
- ES calls this cross field search 
- This will not suffer from these problems.
- Empirically, using the cross field searcher gives a 10% increase in result quality on the test set.
- The code allows for easy switching between the two methods. 

- optimizing for the last fm dataset: 96% accuracy. However, the hand crafted accuracy becomes 52%
- optimizing for the hand crafted dataset: 82% accuracy. However, the last fm accuracy becomes 50% 

## the search controller
- the frontend uses a language that compiles to javascript: elm

# future applications for this solution
- a table based database can be searched very effectively by throwing fields
together in a bag-of-words model
- there is a similar problem with books

## Recommendations
- more test cases
- spelling correction, remove stop words
- include IMSLP, wikipedia works
- combine different works that are actually the same

# process
- process spanned over three years
- development mostly end of 2017, start of 2018
- first read book
- then implemented own search engine
- then used elasticsearch
- original purpose never tested
- no access to historical newspapers
- try to use method for muziekweb
- no significant interest
- not really a purpose for the search engine
- learn about elasticsearch, java, postgres, docker, elm