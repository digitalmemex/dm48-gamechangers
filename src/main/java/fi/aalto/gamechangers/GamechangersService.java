package fi.aalto.gamechangers;

import java.util.List;

public interface GamechangersService {
	
	public static final String NS_PREFIX = "fi.aalto.gamechangers";

	List<Event> getEvents();
	
	List<Work> getWorks();
	
	List<Brand> getBrands();
	
	List<Group> getGroups();
	
	List<Person> getPersons();
	
	List<Comment> getComments();
	
	List<Proposal> getProposals();
	
	interface Event {}	
	interface Work {}	
	interface Brand {}
	interface Group {}
	interface Person {}
	interface Comment {}
	interface Proposal {}
	
}