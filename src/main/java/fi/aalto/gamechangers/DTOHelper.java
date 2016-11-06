package fi.aalto.gamechangers;

import static fi.aalto.gamechangers.GamechangersPlugin.NS;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import de.deepamehta.core.ChildTopics;
import de.deepamehta.core.RelatedTopic;
import de.deepamehta.core.Topic;
import de.deepamehta.core.model.ChildTopicsModel;
import de.deepamehta.core.model.SimpleValue;
import de.deepamehta.core.model.TopicModel;
import de.deepamehta.core.service.CoreService;
import de.deepamehta.core.service.ModelFactory;
import de.deepamehta.workspaces.WorkspacesService;
import fi.aalto.gamechangers.GamechangersService.Brand;
import fi.aalto.gamechangers.GamechangersService.Comment;
import fi.aalto.gamechangers.GamechangersService.CommentBean;
import fi.aalto.gamechangers.GamechangersService.Event;
import fi.aalto.gamechangers.GamechangersService.Group;
import fi.aalto.gamechangers.GamechangersService.Institution;
import fi.aalto.gamechangers.GamechangersService.Person;
import fi.aalto.gamechangers.GamechangersService.Proposal;
import fi.aalto.gamechangers.GamechangersService.ProposalBean;
import fi.aalto.gamechangers.GamechangersService.Work;

public class DTOHelper {
	
	static WorkspacesService wsService;
	
	private static <T> T selfOrDefault(T instance, T defaultValue) {
		return (instance != null) ? instance : defaultValue;
	}
	
	public static Event toEventOrNull(Topic eventTopic) throws JSONException {
		ChildTopics childs = eventTopic.getChildTopics();
		
		if (!selfOrDefault(childs.getBooleanOrNull(NS("event.hidden")), false)) {
			EventImpl dto = new EventImpl();
			dto.put("_type", "event");
			dto.put("id", eventTopic.getId());
			dto.put("name", childs.getStringOrNull("dm4.events.title"));
			dto.put("type", childs.getStringOrNull(NS("event.type")));
			dto.put("address", toAddressOrNull(childs.getTopicOrNull("dm4.contacts.address")));
			dto.put("from", toMillisSinceEpochOrNull(childs.getTopicOrNull("dm4.datetime#dm4.events.from")));
			dto.put("to", toMillisSinceEpochOrNull(childs.getTopicOrNull("dm4.datetime#dm4.events.to")));
			dto.put("notes", childs.getStringOrNull("dm4.events.notes"));
			dto.put("url", childs.getStringOrNull("dm4.webbrowser.url"));
	
			return dto;
		} else {
			return null;
		}
	}
	
	public static Institution toInstitution(Topic instTopic) throws JSONException {
		ChildTopics childs = instTopic.getChildTopics();
		
		InstitutionImpl dto = new InstitutionImpl();
		dto.put("_type", "institution");
		dto.put("id", instTopic.getId());
		dto.put("name", childs.getStringOrNull("dm4.contacts.institution_name"));
		dto.put("type", childs.getStringOrNull(NS("institution.type")));
//		dto.put("address", toAddressOrNull(childs.getTopicOrNull("dm4.contacts.address")));
		dto.put("urls", toStringListOrNull(childs.getTopicsOrNull("dm4.webbrowser.url")));
		dto.put("notes", childs.getStringOrNull("dm4.contacts.notes"));
		dto.put("from", toMillisSinceEpochOrNull(childs.getTopicOrNull("dm4.datetime.date#dm4.events.from")));
		dto.put("to", toMillisSinceEpochOrNull(childs.getTopicOrNull("dm4.datetime.date#dm4.events.to")));
		
		return dto;
	}

	
	public static Group toGroup(Topic groupTopic) throws JSONException {
		ChildTopics childs = groupTopic.getChildTopics();
		
		GroupImpl dto = new GroupImpl();
		dto.put("_type", "group");
		dto.put("id", groupTopic.getId());
		dto.put("name", childs.getStringOrNull(NS("group.name")));
		dto.put("notes", childs.getStringOrNull("dm4.notes.text"));
		dto.put("from", toMillisSinceEpochOrNull(childs.getTopicOrNull("dm4.datetime.date#dm4.events.from")));
		dto.put("to", toMillisSinceEpochOrNull(childs.getTopicOrNull("dm4.datetime.date#dm4.events.to")));

		return dto;
	}
	
	public static Brand toBrand(Topic brandTopic) throws JSONException {
		ChildTopics childs = brandTopic.getChildTopics();
		
		BrandImpl dto = new BrandImpl();
		dto.put("_type", "brand");
		dto.put("id", brandTopic.getId());
		dto.put("name", childs.getStringOrNull(NS("brand.name")));
		dto.put("notes", childs.getStringOrNull("dm4.notes.text"));
		dto.put("from", toMillisSinceEpochOrNull(childs.getTopicOrNull("dm4.datetime.date#dm4.events.from")));
		dto.put("to", toMillisSinceEpochOrNull(childs.getTopicOrNull("dm4.datetime.date#dm4.events.to")));

		return dto;
	}

	public static Comment toCommentOrNull(Topic commentTopic) throws JSONException {
		return toCommentImpl(commentTopic, false);
	}

	public static Comment toComment(Topic commentTopic) throws JSONException {
		return toCommentImpl(commentTopic, true);
	}
	
	private static Comment toCommentImpl(Topic commentTopic, boolean alwaysCreate) throws JSONException {
		ChildTopics childs = commentTopic.getChildTopics();

		if (alwaysCreate || selfOrDefault(childs.getBooleanOrNull(NS("comment.public")), false)) {
			CommentImpl dto = new CommentImpl();
			dto.put("_type", "comment");
			dto.put("id", commentTopic.getId());
			dto.put("name", childs.getStringOrNull("dm4.contacts.person_name"));
			dto.put("notes", childs.getStringOrNull("dm4.notes.text"));
	
			return dto;
		} else {
			return null;
		}
	}
	
	public static Topic toCommentTopic(CoreService dm4, ModelFactory mf, CommentBean comment, Topic topicCommentOn) throws JSONException {
		// TODO: Check input
		
		ChildTopicsModel childs = mf.newChildTopicsModel();
		childs.put("dm4.contacts.person_name", toPersonNameTopicModel(dm4, mf, comment.name));
		childs.put("dm4.contacts.email_address", comment.email);
		childs.put("dm4.notes.text", comment.notes);
		
		Topic topic = dm4.createTopic(mf.newTopicModel(NS("comment"), childs));

		// Sets the relation to the item that is being commented on
		dm4.createAssociation(mf.newAssociationModel("dm4.core.association",
    			mf.newTopicRoleModel(topicCommentOn.getId(), "dm4.core.default"),
			mf.newTopicRoleModel(topic.getId(), "dm4.core.default"))); 
	
		return topic;
	}
	
	private static TopicModel toPersonNameTopicModel(CoreService dm4, ModelFactory mf, String nameString) {
		nameString = nameString.trim();
		
		String firstName;
		String lastName;
		int spaceIndex = -1;
		
		// If a space is available, it'll be in the middle (because of trim())
		if ((spaceIndex = nameString.indexOf(' ')) > -1) {
			firstName = nameString.substring(0, spaceIndex);
			lastName = nameString.substring(spaceIndex + 1);
		} else {
			// Just put everything in the first name
			firstName = nameString;
			lastName = "";
		}

		ChildTopicsModel childs = mf.newChildTopicsModel();
				
		childs.put("dm4.contacts.first_name", firstName);
		childs.put("dm4.contacts.last_name", lastName);
				
		return mf.newTopicModel("dm4.contacts.person_name", childs);
	}
	
	public static Person toPerson(Topic personTopic) throws JSONException {
		ChildTopics childs = personTopic.getChildTopics();
		
		PersonImpl dto = new PersonImpl();
		dto.put("_type", "person");
		dto.put("id", personTopic.getId());
		dto.put("name", childs.getStringOrNull("dm4.contacts.person_name"));
		dto.put("notes", childs.getStringOrNull("dm4.contacts.notes"));
		dto.put("urls", toStringListOrNull(childs.getTopicsOrNull("dm4.webbrowser.url")));
		dto.put("from", toMillisSinceEpochOrNull(childs.getTopicOrNull("dm4.datetime.date#dm4.contacts.date_of_birth")));
		dto.put("to", toMillisSinceEpochOrNull(childs.getTopicOrNull("dm4.datetime.date#" + NS("date_of_death"))));

		return dto;
	}
	
	public static Topic toProposalTopic(CoreService dm4, ModelFactory mf, ProposalBean proposal) throws JSONException {
		// TODO: Check input
		
		ChildTopicsModel childs = mf.newChildTopicsModel();
		childs.put("dm4.contacts.person_name", toPersonNameTopicModel(dm4, mf, proposal.name));
		childs.put("dm4.contacts.email_address", proposal.email);
		childs.put("dm4.notes.text", proposal.notes);
		childs.putRef("dm4.datetime.date#dm4.events.from", toDateTopicModel(dm4, mf, proposal.from).getId());
		childs.putRef("dm4.datetime.date#dm4.events.to", toDateTopicModel(dm4, mf, proposal.to).getId());
		
		return dm4.createTopic(mf.newTopicModel(NS("proposal"), childs));
	}
	
	public static Proposal toProposal(Topic proposalTopic) throws JSONException {
		ChildTopics childs = proposalTopic.getChildTopics();
		
		ProposalImpl dto = new ProposalImpl();
		dto.put("_type", "proposal");
		dto.put("id", proposalTopic.getId());
		dto.put("name", childs.getStringOrNull("dm4.contacts.person_name"));
		dto.put("notes", childs.getStringOrNull("dm4.notes.text"));
		dto.put("from", toMillisSinceEpochOrNull(childs.getTopicOrNull("dm4.datetime.date#dm4.events.from")));
		dto.put("to", toMillisSinceEpochOrNull(childs.getTopicOrNull("dm4.datetime.date#dm4.events.to")));

		return dto;
	}
	
	public static Work toWork(Topic workTopic) throws JSONException {
		ChildTopics childs = workTopic.getChildTopics();
		
		WorkImpl dto = new WorkImpl();
		dto.put("_type", "work");
		dto.put("id", workTopic.getId());
		dto.put("type", childs.getStringOrNull(NS("work.type")));
		dto.put("name", childs.getStringOrNull(NS("work.label")));
		dto.put("notes", childs.getStringOrNull("dm4.notes.text"));
		dto.put("from", toMillisSinceEpochOrNull(childs.getTopicOrNull("dm4.datetime.date#dm4.events.from")));
		dto.put("to", toMillisSinceEpochOrNull(childs.getTopicOrNull("dm4.datetime.date#dm4.events.to")));

		return dto;
	}
	
	private static List<String> toStringListOrNull(List<RelatedTopic> topics) {
		if (topics != null && topics.size() > 0) {
			List<String> list = new ArrayList<String>();
			for (Topic t : topics) {
				list.add(t.getSimpleValue().toString());
			}

			return list;
		} else {
			return null;
		}
	}

	public static TopicModel toDateTopicModel(CoreService dm4, ModelFactory mf, long millis) throws JSONException {
		Calendar cal = Calendar.getInstance();
		
		cal.setTimeInMillis(millis);

		ChildTopicsModel childs = mf.newChildTopicsModel();
		
		putRefOrCreate(dm4, mf, childs, "dm4.datetime.year", cal.get(Calendar.YEAR));
		putRefOrCreate(dm4, mf, childs, "dm4.datetime.month", cal.get(Calendar.MONTH));
		putRefOrCreate(dm4, mf, childs, "dm4.datetime.day", cal.get(Calendar.DAY_OF_MONTH));
		
		Topic t = dm4.createTopic(mf.newTopicModel("dm4.datetime.date", childs));

		assignToCommentsWorkspace(t);
		
		return t.getModel();
	}

	private static void putRefOrCreate(CoreService dm4, ModelFactory mf, ChildTopicsModel childs, String typeUri, Object value) {
		SimpleValue sv = new SimpleValue(value);
		List<Topic> results = dm4.getTopicsByType(typeUri);
		for (Topic t : results) {
			if (t.getSimpleValue().equals(sv)) {
				childs.putRef(typeUri, t.getId());
				
				return;
			}
		}
		
		childs.put(typeUri, value);
	}
	
	private static void assignToCommentsWorkspace(Topic topic) {
		// Assigns the new value to the 'data' workspace
		long wsId = wsService.getWorkspace(NS("workspace.comments")).getId();
		wsService.assignToWorkspace(topic, wsId);
	}
	
	private static Long toMillisSinceEpochOrNull(Topic datetimeTopic) throws JSONException {
		if (datetimeTopic == null) {
			return null;
		}
		ChildTopics childs = datetimeTopic.getChildTopics();

		Topic dateTopic;
		Topic timeTopic;
		
		int year = -1, month = 1, day = 1;
		int hour = 0, minute = 0;

		// dateTimetopic is either dm4.datetime or dm4.datetime.date
		if ("dm4.datetime".equals(datetimeTopic.getTypeUri())) {
			// Retrieves the children
			dateTopic = childs.getTopicOrNull("dm4.datetime.date");
			timeTopic = childs.getTopicOrNull("dm4.datetime.time");
		} else {
			// Initializes dateTopic from datetimeTopic
			dateTopic = datetimeTopic;
			timeTopic = null;
		}
		
		if (dateTopic != null && (childs = dateTopic.getChildTopics()) != null) {
			year = getInt(childs, "dm4.datetime.year", -1);
			month = getInt(childs, "dm4.datetime.month", 1);
			day = getInt(childs, "dm4.datetime.day", 1);
		}

		if (timeTopic != null && (childs = timeTopic.getChildTopics()) != null) {
			hour = getInt(childs, "dm4.datetime.hour", 0);
			minute = getInt(childs, "dm4.datetime.minute", 0);
		}
		
		// At least the year needs to have been specified
		if (year != -1) {
			Calendar cal = Calendar.getInstance();

			cal.set(Calendar.YEAR, year);
			cal.set(Calendar.MONTH, month);
			cal.set(Calendar.DAY_OF_MONTH, day);
			
			cal.set(Calendar.HOUR, hour);
			cal.set(Calendar.MINUTE, minute);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
					
			return cal.getTimeInMillis();
		} else {
			return null;
		}
	}
	
	private static int getInt(ChildTopics childs, String assocDefUri, int defaultValue) {
		Integer value = childs.getIntOrNull(assocDefUri);
		
		return (value != null) ? value.intValue() : defaultValue;
	}
	
	private static JSONObject toAddressOrNull(Topic addressTopic) throws JSONException {
		if (addressTopic == null) {
			return null;
		}
		
		ChildTopics childs = addressTopic.getChildTopics();
		
		JSONObject addr = new JSONObject();
		addr.put("street", childs.getStringOrNull("dm4.contacts.street"));
		addr.put("postal_code", childs.getStringOrNull("dm4.contacts.postal_code"));
		addr.put("city", childs.getStringOrNull("dm4.contacts.city"));
		addr.put("country", childs.getStringOrNull("dm4.contacts.country"));
		
		return addr;
	}
	
	private static class EventImpl extends JSONEnabledImpl implements Event {
	}

	private static class InstitutionImpl extends JSONEnabledImpl implements Institution {
	}

	private static class WorkImpl extends JSONEnabledImpl implements Work {
	}

	private static class BrandImpl extends JSONEnabledImpl implements Brand {
	}

	private static class GroupImpl extends JSONEnabledImpl implements Group {
	}

	private static class PersonImpl extends JSONEnabledImpl implements Person {
	}

	private static class CommentImpl extends JSONEnabledImpl implements Comment {
	}

	private static class ProposalImpl extends JSONEnabledImpl implements Proposal {
	}

}
