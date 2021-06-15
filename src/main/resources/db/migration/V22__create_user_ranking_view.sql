create or replace view user_ranking_progression as
(

select userid, user_ranking, action_taken, transaction_outcome
from execution_history
where userid in (select userid
                 from (select count(userid) as user_count, userid from execution_history group by userid) as temp
                 where user_count > 1)
group by userid, user_ranking, action_taken, transaction_outcome);