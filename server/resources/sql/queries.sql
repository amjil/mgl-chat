-- Place your queries here. Docs available https://www.hugsql.org/

-- :name query-communities :? :*
-- :doc query communities
select a.*, 
        case when b.user_id is null then 0
            else 1 end as is_member
    from communities a 
    left join memberships b on a.id = b.comm_id and b.user_id = :user_id

-- :name query-channel-messages :? :*
-- :doc query channel messages
select a.*, b.screen_name
    from messages a 
    left join users b on a.from_user_id = b.id
where 1 = 1
/*~ (if (true? (:newer? params)) */
and a.created_at > :created_at
/*~*/
and a.created_at < :created_at
/*~ ) ~*/

limit 20