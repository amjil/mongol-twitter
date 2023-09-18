-- Place your queries here. Docs available https://www.hugsql.org/
-- :name query-followed-tweets :? :*
-- :doc query followed tweets
select 
    a.id,
    a.content,
    a.favorites_count,
    a.replies_count,
    a.user_id,
    c.screen_name,
    c.profile_image_url,
    c.sex,
    d.media_links,
    d.link,
    case when g.tweet_id is null 
      then false
      else true end as i_favorites,
    a.created_at
from tweets a 
  left join followers b on a.user_id = b.followee_id 
  left join user_info c on a.user_id = c.id
  left join tweet_entities d on a.id = d.tweet_id 
  left join replies e on e.reply_id = a.id
  left join tweets f on f.id = e.tweet_id
  left join favorites g on g.tweet_id = a.id and g.user_id = :user_id
where 1 = 1
  and (follower_id = :user_id or a.user_id = :user_id)
order by a.created_at desc
limit :limit
offset :offset

-- :name query-tweet-replies :? :*
-- :doc query tweet replies
select 
    a.id,
    a.content,
    a.favorites_count,
    a.replies_count,
    a.user_id,
    c.screen_name,
    c.profile_image_url,
    c.sex,
    d.media_links,
    d.link,
    a.created_at
from tweets a 
  left join user_info c on a.user_id = c.id
  left join tweet_entities d on a.id = d.tweet_id 
  left join replies e on e.reply_id = a.id
where 1 = 1
  and e.tweet_id = :tweet-id
order by a.created_at desc
limit :limit
offset :offset

-- :name user-profile-with-following :? :1
-- :doc user profile with following
select 
  a.profile_image_url, 
  a.sex, 
  a.followings_count, 
  a.profile_banner_url,
  a.screen_name, 
  a.bio, 
  a.birth_date, 
  a.location,
  a.followers_count, 
  a.id,
  case when b.follower_id is null then 0
    else 1 end as following
from user_info a 
  left join followers b on a.id = b.followee_id and b.follower_id = :user_id
where a.id = :id


-- :name query-latest-ntfs :? :*
-- :doc query latest ntfs
select 
  a.id,
  a.from_user_id
  a.content,
  a.types_of,
  a.ntf_id,
  a.created_at,
  b.screen_name
from notifications a 
  left join user_info b on a.from_user_id = b.id
where a.to_user_id = :user_id
  and a.created_at > :last_time

-- :name delete-old-ntfs :? :*
-- :doc delete old ntfs
delete from notifications 
where to_user_id = :user_id 
  and created_at <= :last_time