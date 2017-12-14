## Musicbrainz Search

A simple search engine for the musicbrainz "work" database. 
Each work has a big range of keywords associated with it, ranging from the name of the composer to the name of the work on every album in the database.

### Installation

#### Download a virtual machine
Download a fully functioning vm [here](https://drive.google.com/file/d/1HT7yKCQHXE8K3lXn8KVJ9Gck9XSZq_7t/view?usp=sharing). 
Username is mvanniekerk and password is musicbrainz. 
Try the following query to see a result.
```http request
http://localhost/api/haydn cello concerto d
``` 

#### Install from source
*Note that this is no longer as straightforward.*
*For the api to work, the whole musicbrainz data dump must be imported in the database volume.*

It is also possible to run this on another machine. 
Make sure docker and docker-compose are installed.

```bash
cd musicbrainz_search
docker-compose up --build -d
``` 

Now copy the [up-to-date dump file](https://drive.google.com/file/d/1yFh2NsVqySOsIZHcKO-kD0RnLZx23Z6A/view?usp=sharing) to the machine. Make sure psql for postgres 9.5 is installed on the machine.

```bash
create_db search
pg_restore -d search -h localhost -U musicbrainz search.sql
```