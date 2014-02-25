package behaviour;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import agent.HeroAgent;

@SuppressWarnings("serial")
public class StartBehaviour extends SimpleBehaviour
{
	private boolean start;
	private HeroAgent agent;
	
	public StartBehaviour(Agent a)
	{
		this.myAgent = (HeroAgent) a;
		start = false;
		agent = (HeroAgent) a;
	}
	
	public void action()
	{
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("hero");
		template.addServices(sd);
		try {
			DFAgentDescription[] agents = DFService.search(myAgent, template); 
			if (agents.length == 2)
			{
				for (int i = 0; i < agents.length; i++)
					if (!this.myAgent.getName().equals(agents[i].getName().getName()))
						agent.setFriend(agents[i].getName());
				start = true;
			}
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}
	
	public boolean done()
	{
		return start;
	}
	
	public int onEnd()
	{
		myAgent.addBehaviour(new ListenBehaviour(myAgent));
		myAgent.addBehaviour(new ActBehaviour(myAgent));
		return 0;
	}
}