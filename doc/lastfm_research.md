
## check out
- sparsity last fm dataset
- sparsity last fm dataset after resolving for work
- sparsity last fm dataset when only looking at classical works
- sparsity mmtd after resolving with db (TODO)





number of lastfm tracks with track in mb: 17 million. 
When aggregating for track, the number of times a track occurs in the dataset:

```sql
select
  min(count) as min,
  max(count) as max,
  avg(count) as avg,
  stddev(count) as stddev,
  count(count) as count
from
  (
    select
      count(*) as count,
      trackgid
    from lastfm_user_work
    group by trackgid
  ) as counts;
```

min | max | average | standard deviation | count 
| --- | --- | ------- | ------------------ | ----- |
1 | 3991 | 17.6344239183175379 | 65.4608326984387993 | 960402

An average track is listened to 17 times. Most likely following some kind of pareto distribution. 

```sql
select
  min(count) as min,
  max(count) as max,
  avg(count) as avg,
  stddev(count) as stddev,
  count(count) as count
from
  (
    select count(*) as count,
      workgid
    from lastfm_user_work
      where workgid is not null
    group by workgid
  ) as counts;
```
min | max | average | standard deviation | count 
| --- | --- | ------- | ------------------ | ----- |
1 | 5839 | 55.5700877384706654 | 154.280090029713 | 160135

When converting tracks to works the average work is listened to 55 times. To do this analysis I had to remove a large part of the dataset (8106098 listen events of a total of 17004814) because those recordings are not resolvable to works. Is the improvement just because of popularity bias (more popular recordings have a higher chance of being resolvable to a work) or because work aggregates multiple recordings?
```sql
select
  min(count) as min,
  max(count) as max,
  avg(count) as avg,
  stddev(count) as stddev,
  count(count) as count
from
  (
    select
      count(*) as count,
      trackgid
    from lastfm_user_work
      where workgid is not null
    group by trackgid
  ) as counts;
  ```
  
min | max | average | standard deviation | count 
| --- | --- | ------- | ------------------ | ----- |
1 | 3991 | 40.0873760603289441 | 119.661341050251 | 221983

The first analysis but just with tracks that can be resolved to works. So the difference is a combination of popularity bias and a real effect. The biggest part is popularity bias, but another big part is work aggregation.


Only including works that have a high chance to be a classical work. All (most) classical works have a composer, so lets only include the tracks that have a composer and where the composer is tagged to be classical.
```sql
select distinct listens.*
from
  (select
     count(*) as listencount,
     workgid
   from lastfm_user_work
   where workgid is not null
   group by workgid
  ) as listens
  join work on listens.workgid = work.gid
  join l_artist_work on work.id = entity1
  join link on l_artist_work.link = link.id
  join artist on entity0 = artist.id
  join artist_tag on artist.id = artist_tag.artist
where link.link_type = 168 and tag = 15 and artist_tag.count > 0
--type 168 = composer, tag 15 = classical, some tags have count 0 meaning they are deleted
```
min | max | average | standard deviation | count | sum
| --- | --- | ------- | ------------------ | ----- | --- |
1 | 1394 | 15.2713273038376925 | 45.6655513574835789 | 8182 | 124950


```sql
select
  min(listencount) as min,
  max(listencount) as max,
  avg(listencount) as avg,
  stddev(listencount) as stddev,
  count(listencount) as count,
  sum(listencount) as sum
from
  (
    select distinct listens.*
    from
      (select
         count(*) as listencount,
         trackgid
       from lastfm_user_work
       where workgid is not null
       group by trackgid
      ) as listens
      left join recording_gid_redirect on recording_gid_redirect.gid = trackgid
      join recording on (trackgid = recording.gid or recording_gid_redirect.new_id=recording.id)
      join l_recording_work on l_recording_work.entity0 = recording.id
      join l_artist_work on l_recording_work.entity1 = l_artist_work.entity1
      join link on l_artist_work.link = link.id
      join artist on l_artist_work.entity0 = artist.id
      join artist_tag on artist.id = artist_tag.artist
    where link.link_type = 168 and tag = 15 and artist_tag.count > 0
  ) as counts
--type 168 = composer, tag 15 = classical, some tags have count 0 meaning they are deleted
```

min | max | average | standard deviation | count
| --- | --- | ------- | ------------------ | ----- |
1 | 212 | 9.2599555286031939 | 25.8875551259541684 | 9894

So on average classical pieces are listened to less. Grouping by work doubles the average, most likely by removing a lot of events that only have one listen, since the number of works, with 8192, is only a little lower than the number of tracks with 9894. This also has to do with a small mistake I made in the second query, that misrepresents the amount of unique classical tracks. The average of this query should be a little lower and the count a little higher. 


## PART 2

How many unique tracks are there per user vs how many unique works are there per user. First we need to correct for tracks that have no work associated.

How many tracks are there per user?

```sql
select
  min(listencount) as min,
  max(listencount) as max,
  avg(listencount) as avg,
  stddev(listencount) as stddev,
  count(listencount) as count,
  sum(listencount) as sum
from
  (
    select
      count(*) as listencount,
      userid
    from
      (select distinct
         userid,
         trackgid
       from lastfm_user_work
      where workgid is not null) as distinctworks
    group by userid
  ) as data
```

min | max | average | standard deviation | count | sum
| --- | --- | ------- | ------------------ | ----- | --- |
1 | 23584 | 1889.7229524772497472 | 2066.962919506873 | 989 | 1868936


How many works are there per user? There are slightly less works in total, since the whole point is to combine tracks. The difference between the amount of tracks is only about 5%. Maybe the difference is bigger for classical tracks?

```sql
select
  min(listencount) as min,
  max(listencount) as max,
  avg(listencount) as avg,
  stddev(listencount) as stddev,
  count(listencount) as count,
  sum(listencount) as sum
from
  (
    select
      count(*) as listencount,
      userid
    from
      (select distinct
         userid,
         trackgid
       from lastfm_user_work
      where workgid is not null) as distinctworks
    group by userid
  ) as data
```

| min | max | average | standard deviation | count | sum |
| --- | --- | ------- | ------------------ | ----- | --- |
1 | 22249 | 1814.6016177957532861 | 1940.782770999348 | 989 | 1794641



Repeat this for only classical tracks and works. How many classical tracks are there per user?

```sql
select
  min(listencount) as min,
  max(listencount) as max,
  avg(listencount) as avg,
  stddev(listencount) as stddev,
  count(listencount) as count,
  sum(listencount) as sum
from
  (
    select
      count(*) as listencount,
      userid
    from
      (select distinct
         userid,
         trackgid
       from lastfm_user_work
         join work on workgid = work.gid
         join l_artist_work on work.id = entity1
         join link on l_artist_work.link = link.id
         join artist on entity0 = artist.id
         join artist_tag on artist.id = artist_tag.artist
      where link.link_type = 168 and tag = 15 and artist_tag.count > 0
      and workgid is not null) as distinctworks
    group by userid
  ) as data
```
  
min | max | average | standard deviation | count | sum
| --- | --- | ------- | ------------------ | ----- | --- |
1 | 932 | 39.0918367346938776 | 74.2922685019453532 | 882 | 34479



How many works are there per user?

min | max | average | standard deviation | count | sum
| --- | --- | ------- | ------------------ | ----- | --- |
1 | 873 | 37.4183673469387755 | 69.8832870239868929 | 882 | 33003


So the difference is minimal. But this is to be expected, since a user is unlikely to listen to multiple different interpretations of the same work.
