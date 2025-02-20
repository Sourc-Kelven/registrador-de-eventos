package mz.com.nlw.events.repo;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import mz.com.nlw.events.dto.SubscriptionRankingItem;
import mz.com.nlw.events.model.Event;
import mz.com.nlw.events.model.Subscription;
import mz.com.nlw.events.model.User;

public interface SubscriptionRepo extends CrudRepository <Subscription, Integer> {
	public Subscription findByEventAndSubscriber(Event evt, User user);

	@Query(value = " select count(subscription_number) as quantidade, indication_user_id, user_name"
			+ " from tbl_subscription inner join tbl_user"
			+ " on tbl_subscription.indication_user_id = tbl_user.user_id"
			+ " where indication_user_id is not null"
			+ " and event_id = :eventId"
			+ " group by indication_user_id"
			+ " order by quantidade desc;", nativeQuery = true)
	
	public List<SubscriptionRankingItem> generateRanking(@Param("eventId") Integer eventId);
}
