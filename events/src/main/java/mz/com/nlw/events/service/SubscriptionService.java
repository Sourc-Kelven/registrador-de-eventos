package mz.com.nlw.events.service;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mz.com.nlw.events.dto.SubscriptionRankingByUser;
import mz.com.nlw.events.dto.SubscriptionRankingItem;
import mz.com.nlw.events.dto.SubscriptionResponse;
import mz.com.nlw.events.exception.EventNotFoundException;
import mz.com.nlw.events.exception.SubscriptionConflictException;
import mz.com.nlw.events.exception.UserIndicadorNotFoundException;
import mz.com.nlw.events.model.Event;
import mz.com.nlw.events.model.Subscription;
import mz.com.nlw.events.model.User;
import mz.com.nlw.events.repo.EventRepo;
import mz.com.nlw.events.repo.SubscriptionRepo;
import mz.com.nlw.events.repo.UserRepo;

@Service
public class SubscriptionService {
	
	@Autowired
	private EventRepo evtRepo;
	
	@Autowired
	private UserRepo userRepo;
	
	@Autowired
	private SubscriptionRepo subRepo;
	
	
	public SubscriptionResponse createNewSubscription(String eventName, User user, Integer userId) {
		
		//recuperar o evento pelo nome
		Event evt = evtRepo.findByPrettyName(eventName);
		if(evt == null) { //caso alternativo 2
			throw new EventNotFoundException("Evento " + eventName + " nao existe");
		}
		
		User userRec = userRepo.findByEmail(user.getEmail());
		
		if (userRec == null) { //caso alternativo 1
			userRec = userRepo.save(user);
		}
		
		
		User indicador =null;
		if(userId != null) {
		indicador = userRepo.findById(userId).orElse(null);
		if(indicador == null) {
			throw new UserIndicadorNotFoundException("Usuario " +userId+ " indicador nao existe");
		}
		}
		
		Subscription subs = new Subscription();
		subs.setEvent(evt);
		subs.setSubscriber(userRec);
		subs.setIndication(indicador);
		
		Subscription tmpSub = subRepo.findByEventAndSubscriber(evt, userRec);
		if(tmpSub != null) { //caso altenativo 3
			throw new SubscriptionConflictException("Ja existe inscricao para o usuario " + userRec.getUserName() + "no evento " + evt.getTitle());
		}
		
		Subscription res = subRepo.save(subs);
		return new SubscriptionResponse(res.getSubscriptionNumber(), "http://imersaojava.com/subscription"+res.getEvent().getPrettyName()+"/"+ res.getSubscriber().getId());
	}
	
	public List<SubscriptionRankingItem> getCompleteRanking(String prettyName){
		Event evt = evtRepo.findByPrettyName(prettyName);
		if(evt == null) {
			throw new EventNotFoundException("Ranking do evento " + prettyName + " nao existe");
		}
		return subRepo.generateRanking(evt.getEventId());
	}
	
	public SubscriptionRankingByUser getRankingByUser(String prettyName, Integer userId) {
		List<SubscriptionRankingItem> ranking = getCompleteRanking(prettyName);
		
		SubscriptionRankingItem item = ranking.stream().filter(i->i.userId().equals(userId)).findFirst().orElse(null);
		if(item == null) {
			throw new UserIndicadorNotFoundException("Nao ha inscricoes com indicacao do usuario" + userId);
		}
		Integer posicao = IntStream.range(0, ranking.size()).filter(pos -> ranking.get(pos).userId().equals(userId))
				.findFirst().getAsInt();
		
		return new SubscriptionRankingByUser(item, posicao+1);
	}

}
