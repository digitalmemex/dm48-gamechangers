package fi.aalto.gamechangers;

import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import de.deepamehta.core.JSONEnabled;

public interface GamechangersService {

	Brand getBrand(long id);

	List<Brand> getBrands();

	Event getEvent(long id);	

	List<Event> getEvents();
	
	Group getGroup(long id);
	
	List<Group> getGroups();

	Institution getInstitution(long id);
	
	List<Institution> getInstitutions();

	Work getWork(long id);
	
	List<Work> getWorks();
	
	Person getPerson(long id);
	
	List<Person> getPersons();
	
	Comment getComment(long id);

	Comment createComment(CommentBean comment);
	
	List<Comment> getComments();

	List<Comment> getCommentsOfItem(long id);
	
	Proposal getProposal(long id);

	Proposal createProposal(ProposalBean proposal);
	
	List<Proposal> getProposals();
	
	interface Event extends JSONEnabled {}
	
	interface Institution extends JSONEnabled {}

	interface Work extends JSONEnabled {}
	
	interface Brand extends JSONEnabled {}
	
	interface Group extends JSONEnabled {}
	
	interface Person extends JSONEnabled {}
	
	public static class CommentBean {
		String name;
		String email;
		String notes;
		long commentedItemId;
		
		public CommentBean(JSONObject o) throws JSONException {
			name = o.getString("name");
			email = o.getString("email");
			notes = o.getString("notes");
			commentedItemId = o.getLong("commentedItemId");
		}
	}
	
	interface Comment extends JSONEnabled {}
	
	public static class ProposalBean {
		String name;
		String email;
		String notes;
		String from;
		String to;
		
		public ProposalBean(JSONObject o) throws JSONException {
			name = o.getString("name");
			email = o.getString("email");
			notes = o.getString("notes");
			from = o.getString("from");
			to = o.getString("to");
		}
	}
	
	interface Proposal extends JSONEnabled {}

	
}
