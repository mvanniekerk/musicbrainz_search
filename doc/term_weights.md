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
* documents: **gid**, doc_len 
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
