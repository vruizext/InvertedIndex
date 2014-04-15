Description
===========

InvertedIndex is a Java library which allows to build a queryable index from a text corpus.

Indexer class is designed to solve a particular problem, where the input is  a TSV file containing one document per line, with
fields separated by tabulators. Then, for every line, a document is created an added to the index. Nevertheless, the
API provided by the library can be easily reused to index text in other formats, as well as additional fields can be
added to the documents indexed, and custom processing and parsing can be defined for every indexed field.

The index data is saved to disk in several files, in plaint text format. The postings data is distributed in blocks,
using the hash code of the terms to generate the key of the block where the term is being indexed. This way, the search
component doesn't need to load the whole index in memory before start searching, which speeds up the search.

Additionally, I've implemented a search client and server that use sockets to connect. This variant is much faster, since
the warming of the index, ie loading the strictly necessary data to open the index and start searching (fields config,
norms and stored fields), only it's done once when the server starts, whereas the standard client has to warm the index
for every query, every time it's executed.


Instructions
------------

The project is configured to compile and run tests with maven2, and it's written using Java 7 syntax.

To start using it, first clone from git
```$ git clone https://github.com/bik1979/InvertedIndex.git ```

Then go to the project folder and run:
```
$ cd InvertedIndex
$ mvn clean install
```

Once the project is built, to start indexing a file, use `index.sh`:
```
$ ./bin/index.sh {path to TSV file}
```
`ìndex.sh` sets the initial JVM heap size to 1GB  and the maximum to 2GB. Allocating this amount of RAM before starting
to index speeds-up the indexing process since the application has enough free RAM to work and no time is lost later to
allocate the RAM on demand.


If you don't have any TSV file, try to download from this link
```
$ wget https://www.dropbox.com/s/omulkumbcxx2jo4/simplewiki.tsv.bz2
$ bunzip2 simplewiki.tsv.bz2
```

When the index is ready, to search a term in the index use `search.sh`:
```
$ ./bin/search.sh {term}
```

To start the search server, open a new terminal window and execute `server.sh`:
```
$ ./bin/server.sh
```

Then in the other terminal window, instead of using `search.sh`, use `client.sh` to send queries to the server.
```
$ ./bin/client.sh {term}
```

Discussion questions
---------------------

### Describe the runtime performance of your solution. What is the complexity? Where are the bottlenecks?

For both index and search components, there will be a common bottleneck, the access to disk through the IO. The fewer
the application writes or reads files, the faster it will run. Therefore I've tried to minimize disk accesses.

Asuming that the complexity of get/put operations in Java HashMap are O(1), the complexity of my implementation would be
as explained below, proportional to the number of terms contained in the corpus.

To populate the inverted index, we need to access three different hash maps for every term processed to get to the postings
list; time is also proportional to the number of documents, since we need to keep in a hash map for every document the norm
of indexed fields and the stored fields.

```
(O(1) + O (1) + O(1)) * Numbers of terms in the corpus + (O(1) + O(1)) * Number of documents in the corpus  (build index)
Average document norm = Numbers of terms in the corpus / Number of documents in the corpus
=> Number of documents in the corpus * (2 * O (1) + 3 * O(1) * Average document norm)
=> O(n)
```

To save data to disk, we need to iterate over the entries of the previously populated hash maps,  which is also
proportional to number of documents and to the number of terms in the corpus. To write the postings list, it's necessary
to iterate over all terms in the dictionary, then for every term iterate over its postings list. Besides the postings
list, the norms and the stored fields have to be written to disk and therefore it's necessary to iterate over these
hash maps. The time required will be also proportional to number of documents in the corpus.

```
(O(1) * Number of indexed terms * Average document frequency) + 2 * O(1) * Number of documents in the corpus (write to disk)
=> O(n)
```

So, the indexing time is growing linearly with the number of documents in the corpus. The bottleneck here is, documents
are processed and written to disk sequentially, therefore for a bigger corpus time is going to be much bigger. Parallel
 processing would help in this case to reduce the indexing time.

For the search, there's a constant time required to load norms and stored fields, which is proportional to the number of
document in the corpus (1), plus the time to load the postings list for the searched term, which requires to get values
from 3 hash maps plus iterating over postings list (2). As shown below, this is also growing linearly with the number of
document in the corpus.

```
(2 * O(1) * Number of documents) (1)
+ (O(1) +  O(1) + O(1)  + O(1) * Number of terms in the block + O(1) * number of postings for term) (2)
 => O(n)
```

The bottleneck in the search is therefore the warming-up required. Therefore the server-client alternative is faster,
because it doesn't need  to load the list of norms and stored fields for every search, and then the complexity will be
proportional to the number of terms per block and the number of postings per term (document frequency of the term),
which will be significantly smaller than the total number of documents.



### Is your solution scalable to handle large corpora?

As mentioned above, for large corporas, the indexing time is growing linearly, and could be quite high because the
solution implemented process and writes data to disk sequentially, which could be improved using multi-threads ie parallel
processing.

Besides that, the whole inverted index is maintained in memory during the indexing process. That means, for very large
corpora the JVM could run out of memory if not enough memory is available. Even though more memory could
be allocated using *Xmx* option and nowadays RAM it's affordable, the memory footprint of the application will affect
to the performance. The Garbage Collector will have more work to do, ie more CPU cycles spent, and also, last but not
least, the index data is maintained with hash maps, which teoretically have a complexity of O(1), but when the capacity
grows, this is not always true, since there is higher probability of collisions when accessing a key of the map, the access
time could increase, and moreover if the map keeps on growing, it'll be necessary resizing, which also slows down the process.

For the search component, to handle larger corpora it will be necessary to split the postings dictionary across a higher
number of blocks. In other case, the blocks would become too big, and the time required to load them would increase and
therefore the total time required to perform a query.



### What are the limitations of your ranking strategy?

My scoring algorithm assigns score proportionally to the term frequency and inverse proportionally to the norm of the
document. That means, the more frequent the term appears in the document, the higher the score, and the shorter the
article body, the higher the score. That is taken from Information Retrieval theory, where it's considered that the
relevancy of a term inside a document is proportional to its term frequency, and also it's considered that an occurrence
of a term in a short text has more relevance than in a long one. This can be also seen like a "normalization", since the
probability of a term to occur in a long text will be higher than in a short one, normalizing with the lenght of the text
provides a more accurate measure of how relevant the term in the document is.

Nevertheless, this ranking strategy is very naive, just a very simplified statistical model. It would work fine, if
all the documents have similar length, ie in the interval [mean - sd, mean + sd]. In other case, if there are texts much
shorter than the average where one term occurs, the score will be much higher even though the difference of relevancy
might be not so high, and long documents will tend to get lower scores.

Another limitation of this model is, that it assumes that terms are statistically independent, but actually they're
dependent of the context. And depending on the context the relevance of the term might be different, for example for
words that can get different meanings, one of the meanings could be more relevant than the others.

Another limitation of this scoring algorithm is that it doesn't take in account the order of the terms in the document.
One might say that terms matching at the beginning of the article, or in the title are more relevant than matches in
other parts of the text.

It's also worth on mention that, even though the traditional IR systems use also IDF (inverse document frequency) to score
the relevancy, I've considered that for the aim of this project it was not usefull at all, since the requirements say
that the queries are formed by only one term. Therefore, the IDF is going to be the same for all document that mach the
term, and it's not providing any information. IDF would be useful for queries of several terms. In that case, the documents
matching the term with higher IDF (lower document frequency) would be considered to be more relevant than the matches of
other terms with lower IDF.


### If you had more time, what improvements would you make, and in what order of priority?

* To get a better efficiency and performance, I would implement a new storage strategy using a binary format to write/read
index files, and would access files randomly instead of sequentially or loading the whole file in memory like I do right now.
For this, it would be necessary to store in a new file the offsets where the binary files have to be accessed. For example,
in the case of postings list, we would have a dictionary containing for every term, the offset where to find it.

* In order to make my solution scalable, I would go for a solution which would allow to flush the index to disk
periodically, when certain number of documents have been indexed, unloading then this content of memory. This way, it
wouldn't be necessary to maintain the whole index loaded in memory and it would be possible to index very large corporas
without running out of memory. To achieve this, it would be necessary to implement a different strategy for writing and
reading index files. The current implementation just writes all the postings data to disk, whereas for this new approach
it would be necessary to merge the data in memory with the already existing data in the index files.


* Another improvement which would help to reduce the time required for indexing would be to implement a multi-threaded
 indexer, which could index several documents at time. To implement this, it would be necessary to synchronize
the access to disk files, or provide some kind of mechanishm which provides atomic write/read operations. One possible
solution to this problem would be to process and index text in parallel, but provide a common service to access to disk,
which would have a FIFO queue. So indexing could happen independently of writing data to disk.

* To improve the precission of the search component, I would implement multi-term queries. This would require to use
several threads to make queries for single terms in parallel, and then join the results of the single queries