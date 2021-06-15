create or replace view transaction_ranking_progression as
(

select transaction_id,
       transaction_commit_ranking,
       transaction_system_ranking,
       transaction_eff_ranking,
       action_taken,
       transaction_outcome
from execution_history
where transaction_id in (select transaction_id
                         from (select count(transaction_id) as transaction_count, transaction_id
                               from execution_history
                               group by transaction_id) as temp
                         where transaction_count > 1)
group by transaction_id, transaction_commit_ranking, transaction_system_ranking, transaction_eff_ranking, action_taken,
         transaction_outcome);