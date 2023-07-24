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
    a.created_at
from tweets a 
  left join followers b on a.user_id = b.followee_id 
  left join user_info c on a.user_id = c.id
where 1 = 1
  and follower_id = :user-id
order by a.created_at desc
limit :limit
offset :offset
