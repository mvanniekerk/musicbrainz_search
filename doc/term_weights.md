* we want to filter out stopwords (and, the): **idf**
* if we have a piece by Mozart, and there exists some composer that has a slight variation, we could see something like composer : {Mozart, Mozart, Mozart, Brahms}. We want Mozart to have higher weight. **tf**
* if we have a well known piece, it may have much more terms, for example: {Mozart, Mozart, Mozart} vs {Mozart}. We want both to have the same ranking: **normalize tf by dividing by term length**
* each term can occur in different subsets: name, artist or composer

So:
* each document/term combination has a frequency
* each document has a length
* each term has a document frequency

3 tables:
* terms: **id**, term, term_freq
* documents: **id**, gid, doc_len 
* doc_term: **term_id**, **doc_id**, **type**, freq

formula for relevancy:
```python
result = 0
for term in search_string:
    tf = freq/doc_len 
    idf = log(total_number_of_documents / term_freq)
    tf_idf = tf * idf
    result += tf_idf
return result
```

Empty tables:
```sql
TRUNCATE documents_terms, documents, terms;
ALTER SEQUENCE documents_id_seq RESTART WITH 1;
ALTER SEQUENCE terms_id_seq RESTART WITH 1;
```


For the table, I gave every term 50 characters. This was not enough for one work name (which is also probably an error in the catalog): Preludeprologueanarchitectsdreamthepainterslinksunsetaerialtalsomewhereinbetweennocturnaerial by Kate Bush. 
For this special case, I decided to just cut to the maximal length of the string, 50 characters.

### database schema creation
```sql
CREATE DATABASE search;
```

```sql
CREATE TABLE terms
(
  id SERIAL PRIMARY KEY,
  term CHAR(50) UNIQUE NOT NULL,
  freq INTEGER NOT NULL
);

CREATE TABLE documents
(
  id SERIAL PRIMARY KEY,
  gid uuid UNIQUE NOT NULL,
  length INTEGER NOT NULL
);

CREATE TABLE documents_terms
(
  term_id INTEGER REFERENCES terms(id),
  document_id INTEGER REFERENCES documents(id),
  freq INTEGER NOT NULL,
  type SMALLINT,
  PRIMARY KEY (term_id, document_id, type)
);
```
