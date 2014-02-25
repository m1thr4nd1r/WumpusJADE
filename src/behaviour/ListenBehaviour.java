package behaviour;

import model.Tile;
import jade.core.Agent;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import agent.HeroAgent;

@SuppressWarnings("serial")
public class ListenBehaviour extends SimpleBehaviour
{
	protected HeroAgent agent;
	
	public ListenBehaviour(Agent a)
	{
		this.myAgent = a;
		agent = (HeroAgent) a;
	}
	
	public void action() 
	{			
		boolean flag = false;
    	SequentialBehaviour seq = new SequentialBehaviour();
    				
		ACLMessage msg = agent.receive();
		
		while (msg != null && msg.getConversationId() != null && msg.getLanguage() == null)
		{
			if (msg.getPerformative() == ACLMessage.REFUSE)
			{
				correctBoard(msg.getContent(), msg.getConversationId());
			}
			else if (msg.getPerformative() == ACLMessage.INFORM)
			{
				if (msg.getConversationId().equals("Test"))
				{
					System.out.println("Achei");
				}					
				else if (msg.getConversationId().equals("Safe") ||
						 msg.getConversationId().equals("Breeze") ||
						 msg.getConversationId().equals("Stench") || 
						 msg.getConversationId().equals("NoPit") ||
						 msg.getConversationId().equals("Glitter") ||
						 msg.getConversationId().equals("NoWumpus") ||
						 msg.getConversationId().equals("WumpusFound"))
				{
					mark(msg);
				}
				else if (msg.getConversationId().equals("Shoot"))
				{
					boolean hit = Boolean.valueOf(msg.getContent());
					
					if (hit)
						updateWumpus("WumpusShot");
				}
				else if (msg.getConversationId().equals("WumpusKilled"))
				{
					updateWumpus("WumpusKilled");
				}
				else if (msg.getConversationId().equals("GoldGrabbed"))
				{
					goldGrabbed(msg.getContent());
				}
				else if (msg.getConversationId().equals("Percept") && !flag)
				{
					flag = true;
					seq.addSubBehaviour(new ThinkBehaviour(agent, msg.getContent()));
					seq.addSubBehaviour(new ActBehaviour(agent));						
				}
			}
			
			msg = agent.receive();
		} 
		
		if (!seq.getChildren().isEmpty())
			agent.addBehaviour(seq);
	}
	
	public void mark(ACLMessage msg)
	{
		String content = msg.getContent();
		String subject = msg.getConversationId();
		String reply = "";
		int begin = 0;
		int end = content.indexOf('|', begin);
		Tile[][] board = agent.getBoard();
		
		do
		{
			int sourceX = Integer.parseInt(content.substring(begin, end));
			
			begin = end + 1;
			end = content.indexOf('|', begin);
			
			int sourceY = Integer.parseInt(content.substring(begin, end));
			
			begin = end + 1;
			end = content.indexOf('|', begin);
			
			if (subject.equals("Safe"))
			{
				board[sourceY][sourceX].setPit(0);
				board[sourceY][sourceX].setWumpus(0);
			}
			else if (subject.equals("Breeze"))
			{
				if (board[sourceY][sourceX].getPit() > 1)
					board[sourceY][sourceX].setPit(2);
				else
					reply = reply.concat(sourceX + "|" + sourceY + "|" + board[sourceY][sourceX].getPit() + "|");
			}
			else if (subject.equals("Stench"))
			{
				if (board[sourceY][sourceX].getWumpus() > 1)
					board[sourceY][sourceX].setWumpus(2);
				else
					reply = reply.concat(sourceX + "|" + sourceY + "|" + board[sourceY][sourceX].getWumpus() + "|");		
			}
			else if (subject.equals("NoPit"))
				board[sourceY][sourceX].setPit(0);
			else if (subject.equals("NoWumpus"))
				board[sourceY][sourceX].setWumpus(0);
			else if (subject.equals("Glitter"))
			{
				if (!agent.getGold())
				{
					board[sourceY][sourceX].setPercept(2, true);
					board[sourceY][sourceX].setVisited(true);
					agent.setGold(true);
					agent.printAction("Gold Found and Grabbed!");
				}
			}
			else if (subject.equals("WumpusFound"))
			{
				board[sourceY][sourceX].setWumpus(1);
				agent.printAction("Wumpus Found!");
			}
			
			agent.setBoard(board[sourceY][sourceX], sourceY, sourceX);
			
		}while (end > 0);
		
		end = content.indexOf('@', begin);
		
		if (end > 0)
		{
			int originX = Integer.parseInt(content.substring(begin, end));
			int originY = Integer.parseInt(content.substring(end + 1));
			
			board[originY][originX].setVisited(true);
			
			agent.setBoard(board[originY][originX], originY, originX);
		}
		
		if (!reply.isEmpty())
		{
			ACLMessage anwser = msg.createReply();
			anwser.setPerformative(ACLMessage.REFUSE);
			anwser.setContent(reply);
			agent.send(anwser);
		}
	}
	
	public void goldGrabbed(String content)
	{
		int sourceX = Integer.parseInt(content.substring(0, content.indexOf('|')));
		int sourceY = Integer.parseInt(content.substring(content.indexOf('|') + 1));
		
		Tile tile = agent.getBoard()[sourceY][sourceX];
		
		tile.setPercept(2, false);
		
		agent.setBoard(tile, sourceY, sourceX);
		
		agent.setGold(true);
	}
	
	public void correctBoard(String content, String subject)
	{
		int begin = 0;
		int end = content.indexOf('|', begin);
		Tile tile;
		
		do
		{
			int sourceX = Integer.parseInt(content.substring(begin, end));
			
			begin = end + 1;
			end = content.indexOf('|', begin);
			
			int sourceY = Integer.parseInt(content.substring(begin, end));
			
			begin = end + 1;
			end = content.indexOf('|', begin);
			
			tile = agent.getBoard()[sourceY][sourceX];
			int value = Integer.parseInt(content.substring(begin, end));
			
			begin = end + 1;
			end = content.indexOf('|', begin);
			
			if (subject.equals("Breeze"))
				tile.setPit(value);
			else if (subject.equals("Stench"))
				tile.setWumpus(value);
			
			agent.setBoard(tile, sourceY, sourceX);
			
		}while (end > 0);
	}
	
	public void updateWumpus(String msg)
	{
		// Por enquanto so se atira se tiver certeza de onde o Wumpus esta
		if (msg.equals("WumpusShot"))
		{
			ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
			inform.setConversationId("WumpusKilled");
			inform.addReceiver(agent.getFriend());
			agent.send(inform);
			
			updateWumpus("WumpusKilled");
		}
		else if (msg.equals("WumpusKilled"))
		{
			agent.printAction("Wumpus Killed");
			
			Tile[][] board = agent.getBoard();
			
			for (int i = 0; i < 4; i++)
	            for (int j = 0; j < 4; j++)
	            {
	                board[i][j].setPercept(4, true);
	                board[i][j].setWumpus(0);
	                
	                agent.setBoard(board[i][j], i, j);
	            }
		}
	}
	
	public boolean done()
	{
		return !agent.isAlive() || ((agent.getX() == 0) && (agent.getY() == 3) && agent.getGold());
	}
}