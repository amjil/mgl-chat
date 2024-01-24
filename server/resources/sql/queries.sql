-- Place your queries here. Docs available https://www.hugsql.org/

-- :name query-communities :? :*
-- :doc query communities
select a.*, 
        case when b.user_id is null then 0
            else 1 end as is_member
    from communities a 
    left join memberships b on a.id = b.comm_id and b.user_id = :user_id
