package behaviour;

import model.Tile;
import agent.HeroAgent;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

@SuppressWarnings("serial")
public class ThinkBehaviour extends OneShotBehaviour
{
	String percepts;
	HeroAgent agent;
	int x, y;
	Tile[][] board;
	
	public ThinkBehaviour(Agent a, String s)
	{
		myAgent = a;
		percepts = s;
		agent = (HeroAgent) a;
		board = agent.getBoard();
	}
	
	public void action() 
	{
		x = agent.getX();
		y = agent.getY();
		
		if (!board[y][x].isVisited())
            board[y][x].setVisited(true);
        
        if (!board[y][x].isSafe())
        {
            board[y][x].setPit(0);
            board[y][x].setWumpus(0);
        }
        
        agent.setBoard(board[y][x], y, x);
        
        if (percepts.length() == 0)
            markSafe();
        else if (percepts.contains("P") || 
    			(percepts.contains("W") && agent.getArrow()))
        	agent.setAlive(false);
        else
        {	
            if (percepts.contains("B"))
            	markNeighbours('B');
            else
                markNeighbours('R');
            
            if (percepts.contains("S"))
            	markNeighbours('S');
            else
                markNeighbours('T');
            
            if (percepts.contains("G"))
            	markNeighbours('G');
        }
        
        if (!agent.getWumpusLocation())
        	updateWumpus();
	}
	
	public void markSafe()
    {
		String content = "";
		ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
		inform.setConversationId("Safe");
		inform.addReceiver(agent.getFriend());
		
        if (y > 0)
        {
            board[y-1][x].setPit(0);
            board[y-1][x].setWumpus(0);
            
            agent.setBoard(board[y-1][x], y-1, x);
            
            content = content.concat(x + "|" + (y - 1) + "|");
        }
        if (y < 3)
        {
            board[y+1][x].setPit(0);
            board[y+1][x].setWumpus(0);
            
            agent.setBoard(board[y+1][x], y+1, x);
            
            content = content.concat(x + "|" + (y + 1) + "|");
        }
        
        if (x > 0)
        {
            board[y][x-1].setPit(0);
            board[y][x-1].setWumpus(0);
            
            agent.setBoard(board[y][x-1], y, x-1);
            
            content = content.concat((x - 1) + "|" + y + "|");
        }
        if (x < 3) 
        {
            board[y][x+1].setPit(0);
            board[y][x+1].setWumpus(0);
            
            agent.setBoard(board[y][x+1], y, x+1);
            
            content = content.concat((x + 1) + "|" + y + "|");
        }
        
        if (!content.isEmpty())
    	{
        	inform.setContent(content);
        	agent.send(inform);
    	}
    }
	
	public void markNeighbours(char code)
    {
		String content = "";
		
		ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
		inform.addReceiver(agent.getFriend());
		
		if (code == 'B')
        {
			inform.setConversationId("Breeze");
			
            if (!board[y][x].getPercept(1))
            {
                board[y][x].setPercept(1, true);
                agent.setBoard(board[y][x],y,x);
            }
            
            if (y > 0)
            	if (board[y-1][x].getPit() == 3)
                {
                    board[y-1][x].setPit(2);
                    agent.setBoard(board[y-1][x],y-1,x);                    
                    
                    content = content.concat(x + "|" + (y - 1) + "|");
                }
            if (y < 3)
            	if (board[y+1][x].getPit() == 3)
                {
                    board[y+1][x].setPit(2);
                    agent.setBoard(board[y+1][x],y+1,x);
                    
                    content = content.concat(x + "|" + (y + 1) + "|");
                }
            if (x > 0)
            	if (board[y][x-1].getPit() == 3)
                {
                    board[y][x-1].setPit(2);
                    agent.setBoard(board[y][x-1],y,x-1);
                    
                    content = content.concat((x - 1) + "|" + y + "|");
                }
            if (x < 3)
            	if (board[y][x+1].getPit() == 3)
                {
                    board[y][x+1].setPit(2);
                    agent.setBoard(board[y][x+1],y,x+1);
                    
                    content = content.concat((x + 1) + "|" + y + "|");
                }
            
            if (!content.isEmpty())
            {
            	content = content.concat(x + "@" + y);
	            inform.setContent(content);	            
	            agent.send(inform);
            }
        }
        else if (code == 'S')
        {
        	inform.setConversationId("Stench");
        	
            if (!board[y][x].getPercept(0))
            {
                board[y][x].setPercept(0, true);
                agent.setBoard(board[y][x],y,x);
            }
            
            if (y > 0)
            	if (board[y-1][x].getWumpus() == 3)
                {
            		board[y-1][x].setWumpus(2);
            		agent.setBoard(board[y-1][x],y-1,x);
            		
                    content = content.concat(x + "|" + (y - 1) + "|");
                }
            if (y < 3)
            	if (board[y+1][x].getWumpus() == 3)
                {
                    board[y+1][x].setWumpus(2);
                	agent.setBoard(board[y+1][x],y+1,x);
                	
                    content = content.concat(x + "|" + (y + 1) + "|");
                }
            if (x > 0)
            	if (board[y][x-1].getWumpus() == 3)
                {
                    board[y][x-1].setWumpus(2);
                    agent.setBoard(board[y][x-1],y,x-1);

                    content = content.concat((x - 1) + "|" + y + "|");
                }
            if (x < 3)
            	if (board[y][x+1].getWumpus() == 3)
	            {
	                board[y][x+1].setWumpus(2);
	                agent.setBoard(board[y][x+1],y,x+1);
	                
	                content = content.concat((x + 1) + "|" + y + "|");
	            }
            
            if (!content.isEmpty())
            {
            	content = content.concat(x + "@" + y);
            	inform.setContent(content);
            	agent.send(inform);
            }
        }
        else if (code == 'G')
        {
        	board[y][x].setPercept(2, true);
            board[y][x].setPit(0);
            board[y][x].setWumpus(0);
            
            agent.setBoard(board[y][x],y,x);

            inform.setConversationId("Glitter");
        	inform.setContent(x + "|" + y + "|");
        	agent.send(inform);
        }
        else if (code == 'T')
    	// Sem Wumpus ao Redor
        {
        	inform.setConversationId("NoWumpus");
        		    		
    		if (y > 0)
            {
                board[y-1][x].setWumpus(0);
                agent.setBoard(board[y-1][x],y-1,x);

                content = content.concat(x + "|" + (y - 1) + "|");
            }
            if (y < 3)
            {
                board[y+1][x].setWumpus(0);
                agent.setBoard(board[y+1][x],y+1,x);
                
                content = content.concat(x + "|" + (y + 1) + "|");
            }
            if (x > 0)
            {
                board[y][x-1].setWumpus(0);
                agent.setBoard(board[y][x-1],y,x-1);
                
                content = content.concat((x - 1) + "|" + y + "|");
            }
            if (x < 3)
            {	
                board[y][x+1].setWumpus(0);
                agent.setBoard(board[y][x+1],y,x+1);
                
                content = content.concat((x + 1) + "|" + y + "|");
            }
            
            if (!content.isEmpty())
            {
            	content = content.concat(x + "@" + y);
            	inform.setContent(content);
            	agent.send(inform);
            }
        }
        else if (code == 'R')
    	// Sem Pits ao redor
        {
        	inform.setConversationId("NoPit");
        	
            if (y > 0)
            {
                board[y-1][x].setPit(0);
                agent.setBoard(board[y-1][x],y-1,x);
                
                content = content.concat(x + "|" + (y - 1) + "|");
            }
            if (y < 3)
            {
                board[y+1][x].setPit(0);
                agent.setBoard(board[y+1][x],y+1,x);

                content = content.concat(x + "|" + (y + 1) + "|");
            }
            if (x > 0)
            {
                board[y][x-1].setPit(0);
                agent.setBoard(board[y][x-1],y,x-1);

                content = content.concat((x - 1) + "|" + y + "|");
            }
            if (x < 3)
            {
                board[y][x+1].setPit(0);
                agent.setBoard(board[y][x+1],y,x+1);
                
                content = content.concat((x + 1) + "|" + y + "|");
            }
            
            if (!content.isEmpty())
            {
            	content = content.concat(x + "@" + y);
            	inform.setContent(content);
            	agent.send(inform);
            }
        }
    }
	
	// Verifica o board para o caso de so existir um Tile
	// com valor Wumpus == 2
	public void updateWumpus()
    {
        int k = 0,a = 0,b = 0;
        
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                if (board[i][j].getWumpus() == 2)
                {
                    k++;
                    a = j;
                    b = i;
                }
        
        if (k == 1 && !agent.getWumpusLocation())
        {
            board[b][a].setWumpus(1);
            board[b][a].setPit(0);
            agent.setWumpusLocation(true);
            
            agent.setBoard(board[b][a], b, a);
            
            ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
			inform.addReceiver(agent.getFriend());
			inform.setConversationId("WumpusFound");
        	inform.setContent(a + "|" + b + "|");
        	agent.send(inform);
        }
    }
	
	public int onEnd()
	{
		if (!agent.isAlive())
        {
        	agent.printAction("This Agent is Dead");
        	agent.removeBehaviour(this.parent);
//        	this.myAgent.doSuspend();
        }
		
		return 0;
	}
}