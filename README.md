# Musicbrainz Search

A free text search engine for the musicbrainz database. Results are aggregated by work. 
Each work has a big range of keywords associated with it, ranging from the name of the composer to the name of the work on every album in the database.


### Installation

To set up this database you will need to download the [musicbrainz virtual machine](https://musicbrainz.org/doc/MusicBrainz_Server/Setup).
After following the setup instructions on the musicbrainz site you will need to open port 5432 to access the postgres database itself.
You will need to do this once in virtualbox and once more in the docker compose file inside the vm.
More detailed instructions can be found [here](doc/Initial_documentation.ipynb).

This machine will still not give you access to some of the optimisation options that this project provides. 
To get those to work correctly you will need my working copy of the musicbrainz vm. However those
optimizations are not necessary for this project to work.

Once the musicbrainz vm is up and running, you can set up the elasticsearch database.
In the repository main directory: 

```commandline
$ mvn package -DskipTests
$ MB_SEARCH=work-aggregate docker-compose up
```

Running the second command can take a couple of hours. When the aggregation is done, 
you can stop the cluster again. After aggregation, you can run the search engine frontend.

```commandline
$ MB_SEARCH=webserver docker-compose up
```

To find optimal parameters run

```commandline
$ MB_SEARCH=optimize docker-compose up
```