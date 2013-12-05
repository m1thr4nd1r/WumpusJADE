package wumpus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class HeroAgent extends Agent {

	private static final long serialVersionUID = 1L;
	
	private Tile board[][];
    private int x, y, previousSafeX, previousSafeY, id;
    private String orientation;
    private boolean arrow, gold, alive, safe, wumpusLocation;
    private AID friend;
	
    protected void setup()
	{	
    	// Setando o id do agente
        int end = getName().indexOf('@');
		this.id = Integer.parseInt(getName().substring(end - 1, end));
    	
    	// Inicializando Agente (orientacao sempre a direita e
    	// comecando da posicao inicial)
    	this.y = 3;
        this.x = 0;
        
        if (id == 0)
        	this.orientation = "Right";
        else
        	this.orientation = "Up";
        
    	// Inicializando atributos boleanos
        this.arrow = true;
        this.gold = false;
        this.alive = true;
        this.safe = true;
        this.wumpusLocation = false;
        
        // Inicializando a ultima posicao segura (inicialmente zerada)
        this.previousSafeX = -1;
        this.previousSafeY = -1;
        
		// Criando o tabuleiro do jogo
        this.board = new Tile[4][4];
        for (int j = 0; j < 4; j++)
            for (int i = 0; i < 4; i++)
                this.board[j][i] = new Tile();
        
        // Setando o inicio como visitado
        this.board[this.y][this.x].setVisited(true);
        
        // Setando os vizinhos do inicio como seguros
        this.board[this.y][this.x].setWumpus(0);
        this.board[this.y][this.x].setPit(0);
        this.board[this.y-1][this.x].setWumpus(0);
        this.board[this.y-1][this.x].setPit(0);
        this.board[this.y][this.x+1].setWumpus(0);
        this.board[this.y][this.x+1].setPit(0);
        
        // Registrando o Agente no DF
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
		dfd.setName(getAID());
		sd.setType("hero");
		sd.setName("JADE-Wumpus");
		dfd.addServices(sd);
		
		try {
			DFService.register(this, dfd);
			addBehaviour(new StartBehaviour(this));
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
    }
    
    protected void takeDown() 
    {
       try { DFService.deregister(this); }
       catch (Exception e) {}
    }
    
    public void printAction(String action)
    {
        System.out.println();
        System.out.println("Position: X = " + this.x + " Y = " + this.y);
        System.out.println("Orientation: " + this.orientation);
        System.out.println(action);
        System.out.println("------------");
    }
    
	public void printBoard()
	{
		System.out.println("   S B G b s   V P W     S B G b s   V P W     S B G b s   V P W     S B G b s   V P W");
		for (int i = 0 ; i < 4; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				System.out.print(" ");
				if (j != 0)
					System.out.print("| ");
  
				System.out.print("[ ");
  
				for (int k = 0; k < 5; k++)
					if (this.board[i][j].getPercept(k))
						System.out.print("T ");
					else
						System.out.print("F ");
  
				System.out.print("] ");
  
				if (i == y && j == x)
					System.out.print("H ");
				else if (this.board[i][j].isVisited())
					System.out.print("T ");
				else
					System.out.print("F ");
                      
				System.out.print(this.board[i][j].getPit() + " " + this.board[i][j].getWumpus());
			}
			System.out.println();
		}
	}
    
    private class StartBehaviour extends SimpleBehaviour
    {
		private static final long serialVersionUID = 1L;
		private boolean start = false;
		
		public StartBehaviour(Agent a)
		{
			this.myAgent = (HeroAgent) a;
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
							friend = agents[i].getName();
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
    
    private class ListenBehaviour extends SimpleBehaviour
    {
    	private static final long serialVersionUID = 1L;
    	
		public ListenBehaviour(Agent a)
		{
			this.myAgent = (HeroAgent) a;
		}
		
		public void action() 
		{			
			boolean flag = false;
	    	SequentialBehaviour seq = new SequentialBehaviour();
	    				
			ACLMessage msg = receive();
			
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
						seq.addSubBehaviour(new ThinkBehaviour(myAgent, msg.getContent()));
						seq.addSubBehaviour(new ActBehaviour(myAgent));						
					}
				}
				
				msg = receive();
			} 
			
			if (!seq.getChildren().isEmpty())
				myAgent.addBehaviour(seq);
		}
		
		public void mark(ACLMessage msg)
		{
			String content = msg.getContent();
			String subject = msg.getConversationId();
			String reply = "";
			int begin = 0;
			int end = content.indexOf('|', begin);
			
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
				{
					board[sourceY][sourceX].setPit(0);
				}
				else if (subject.equals("NoWumpus"))
				{
					board[sourceY][sourceX].setWumpus(0);
				}
				else if (subject.equals("Glitter"))
				{
					if (!gold)
					{
						board[sourceY][sourceX].setPercept(2, true);
						board[sourceY][sourceX].setVisited(true);
						gold = true;
						printAction("Gold Found and Grabbed!");
					}
				}
				else if (subject.equals("WumpusFound"))
				{
					board[sourceY][sourceX].setWumpus(1);
					printAction("Wumpus Found!");
				}
				
			}while (end > 0);
			
			end = content.indexOf('@', begin);
			
			if (end > 0)
			{
				int originX = Integer.parseInt(content.substring(begin, end));
				int originY = Integer.parseInt(content.substring(end + 1));
				board[originY][originX].setVisited(true);
			}
			
			if (!reply.isEmpty())
			{
				ACLMessage anwser = msg.createReply();
				anwser.setPerformative(ACLMessage.REFUSE);
				anwser.setContent(reply);
				send(anwser);
			}
		}
		
		public void goldGrabbed(String content)
		{
			int sourceX = Integer.parseInt(content.substring(0, content.indexOf('|')));
			int sourceY = Integer.parseInt(content.substring(content.indexOf('|') + 1));
			
			board[sourceY][sourceX].setPercept(2, false);
			gold = true;
		}
		
		public void correctBoard(String content, String subject)
		{
			int begin = 0;
			int end = content.indexOf('|', begin);
			
			do
			{
				int sourceX = Integer.parseInt(content.substring(begin, end));
				
				begin = end + 1;
				end = content.indexOf('|', begin);
				
				int sourceY = Integer.parseInt(content.substring(begin, end));
				
				begin = end + 1;
				end = content.indexOf('|', begin);
				
				int value = Integer.parseInt(content.substring(begin, end));
				
				begin = end + 1;
				end = content.indexOf('|', begin);
				
				if (subject.equals("Breeze"))
					board[sourceY][sourceX].setPit(value);
				else if (subject.equals("Stench"))
					board[sourceY][sourceX].setWumpus(value);
				
			}while (end > 0);
		}
		
		public void updateWumpus(String msg)
		{
			// Por enquanto so se atira se tiver certeza de onde o Wumpus esta
			if (msg.equals("WumpusShot"))
			{
				ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
				inform.setConversationId("WumpusKilled");
				inform.addReceiver(friend);
				send(inform);
				
				updateWumpus("WumpusKilled");
			}
			else if (msg.equals("WumpusKilled"))
			{
				printAction("Wumpus Killed");
				
				for (int i = 0; i < 4; i++)
		            for (int j = 0; j < 4; j++)
		            {
		                board[i][j].setPercept(4, true);
		                board[i][j].setWumpus(0);
		            }
			}
		}
		
		public boolean done()
		{
			return !alive || ((x == 0) && (y == 3) && gold);
		}
    }
    
    private class ThinkBehaviour extends OneShotBehaviour
    {
		private static final long serialVersionUID = 1L;

		String percepts;
					
		public ThinkBehaviour(Agent a, String s)
		{
			myAgent = a;
			percepts = s; 
		}
		
		public void action() 
		{
			if (!board[y][x].isVisited())
	            board[y][x].setVisited(true);
	        
	        if (!board[y][x].isSafe())
	        {
	            board[y][x].setPit(0);
	            board[y][x].setWumpus(0);
	        }
	        
	        if (percepts.length() == 0)
	            markSafe();
	        else if (percepts.contains("P") || 
        			(percepts.contains("W") && arrow))
	        	alive = false;
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
	        
	        if (!wumpusLocation)
	        	updateWumpus();
		}
   	
		public void markSafe()
	    {
			String content = "";
			ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
			inform.setConversationId("Safe");
			inform.addReceiver(friend);
			
	        if (y > 0)
	        {
	            board[y-1][x].setPit(0);
	            board[y-1][x].setWumpus(0);
	            content = content.concat(x + "|" + (y - 1) + "|");
	        }
	        if (y < 3)
	        {
	            board[y+1][x].setPit(0);
	            board[y+1][x].setWumpus(0);
	            content = content.concat(x + "|" + (y + 1) + "|");
	        }
	        
	        if (x > 0)
	        {
	            board[y][x-1].setPit(0);
	            board[y][x-1].setWumpus(0);
	            content = content.concat((x - 1) + "|" + y + "|");
	        }
	        if (x < 3) 
	        {
	            board[y][x+1].setPit(0);
	            board[y][x+1].setWumpus(0);
	            content = content.concat((x + 1) + "|" + y + "|");
	        }
	        
	        if (!content.isEmpty())
        	{
	        	inform.setContent(content);
	        	send(inform);
        	}
	    }
		
		public void markNeighbours(char code)
	    {
			String content = "";
			
			ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
			inform.addReceiver(friend);
			
			if (code == 'B')
	        {
				inform.setConversationId("Breeze");
				
	            if (!board[y][x].getPercept(1))
	                board[y][x].setPercept(1, true);
	            
	            if (y > 0)
	            	if (board[y-1][x].getPit() == 3)
	                {
		                    board[y-1][x].setPit(2);
		                    content = content.concat(x + "|" + (y - 1) + "|");
	                }
	            if (y < 3)
	            	if (board[y+1][x].getPit() == 3)
	                {
	                    board[y+1][x].setPit(2);
	                    content = content.concat(x + "|" + (y + 1) + "|");
	                }
	            if (x > 0)
	            	if (board[y][x-1].getPit() == 3)
	                {
	                    board[y][x-1].setPit(2);
	                    content = content.concat((x - 1) + "|" + y + "|");
	                }
	            if (x < 3)
	            	if (board[y][x+1].getPit() == 3)
	                {
	                    board[y][x+1].setPit(2);
	                    content = content.concat((x + 1) + "|" + y + "|");
	                }
	            
	            if (!content.isEmpty())
	            {
	            	content = content.concat(x + "@" + y);
		            inform.setContent(content);	            
		            send(inform);
	            }
	        }
	        else if (code == 'S')
	        {
	        	inform.setConversationId("Stench");
	        	
	            if (!board[y][x].getPercept(0))
	                board[y][x].setPercept(0, true);
	            
	            if (y > 0)
	            	if (board[y-1][x].getWumpus() == 3)
	                {
	                    board[y-1][x].setWumpus(2);
	                    content = content.concat(x + "|" + (y - 1) + "|");
	                }
	            if (y < 3)
	            	if (board[y+1][x].getWumpus() == 3)
	                {
	                    board[y+1][x].setWumpus(2);
	                    content = content.concat(x + "|" + (y + 1) + "|");
	                }
	            if (x > 0)
	            	if (board[y][x-1].getWumpus() == 3)
	                {
	                    board[y][x-1].setWumpus(2);
	                    content = content.concat((x - 1) + "|" + y + "|");
	                }
	            if (x < 3)
	            	if (board[y][x+1].getWumpus() == 3)
		            {
		                board[y][x+1].setWumpus(2);
		                content = content.concat((x + 1) + "|" + y + "|");
		            }
	            
	            if (!content.isEmpty())
	            {
	            	content = content.concat(x + "@" + y);
	            	inform.setContent(content);
	            	send(inform);
	            }
	        }
	        else if (code == 'G')
	        {
	        	board[y][x].setPercept(2, true);
	            board[y][x].setPit(0);
	            board[y][x].setWumpus(0);
	            
	            inform.setConversationId("Glitter");
	        	inform.setContent(x + "|" + y + "|");
	        	send(inform);
	        }
	        else if (code == 'T')
        	// Sem Wumpus ao Redor
	        {
	        	inform.setConversationId("NoWumpus");
	        		    		
	    		if (y > 0)
	            {
	                board[y-1][x].setWumpus(0);
	                content = content.concat(x + "|" + (y - 1) + "|");
	            }
	            if (y < 3)
	            {
	                board[y+1][x].setWumpus(0);
	                content = content.concat(x + "|" + (y + 1) + "|");
	            }
	            if (x > 0)
	            {
	                board[y][x-1].setWumpus(0);
	                content = content.concat((x - 1) + "|" + y + "|");
	            }
	            if (x < 3)
	            {	
	                board[y][x+1].setWumpus(0);
	                content = content.concat((x + 1) + "|" + y + "|");
	            }
	            
	            if (!content.isEmpty())
	            {
	            	content = content.concat(x + "@" + y);
	            	inform.setContent(content);
	            	send(inform);
	            }
	        }
	        else if (code == 'R')
        	// Sem Pits ao redor
	        {
	        	inform.setConversationId("NoPit");
	        	
	            if (y > 0)
	            {
	                board[y-1][x].setPit(0);
	                content = content.concat(x + "|" + (y - 1) + "|");
	            }
	            if (y < 3)
	            {
	                board[y+1][x].setPit(0);
	                content = content.concat(x + "|" + (y + 1) + "|");
	            }
	            if (x > 0)
	            {
	                board[y][x-1].setPit(0);
	                content = content.concat((x - 1) + "|" + y + "|");
	            }
	            if (x < 3)
	            {
	                board[y][x+1].setPit(0);
	                content = content.concat((x + 1) + "|" + y + "|");
	            }
	            
	            if (!content.isEmpty())
	            {
	            	content = content.concat(x + "@" + y);
	            	inform.setContent(content);
	            	send(inform);
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
	        
	        if (k == 1 && !wumpusLocation)
	        {
	            board[b][a].setWumpus(1);
	            board[b][a].setPit(0);
	            wumpusLocation = true;
	            
	            ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
				inform.addReceiver(friend);
				inform.setConversationId("WumpusFound");
	        	inform.setContent(a + "|" + b + "|");
	        	send(inform);
	        }
	    }
		
		public int onEnd()
		{
			if (!alive)
	        {
	        	printAction("This Agent is Dead");
	        	this.myAgent.removeBehaviour(this.parent);
//	        	this.myAgent.doSuspend();
	        }
			
			return 0;
		}
    }
    
    private class ActBehaviour extends OneShotBehaviour
    {
		private static final long serialVersionUID = 1L;
		
		public ActBehaviour(Agent a)
		{
			this.myAgent = a;
		}
		
		public void action() 
		{
			int[] pos = new int[2];
	        pos = unvisitedNeighbour();
	    
	        if (board[y][x].getPercept(2))
	        {
	            grab();
	            printAction("Grabbing Gold");
	        }
	        else 
	        {
	        	String action = canShoot();
	        	
	        	if (!action.isEmpty())
	        	{
		        	if (!action.equals("OK"))
		        		orientation = rotate(orientation, action);
		        	else
		        	{
		        		// Caso o agente atire, entao a mensagem e enviada ao WorldAgent
		        	    // para processamento
		        		shoot();
		        	}
	        	}
	        	else if (pos[0] != -1 && pos[1] != -1)
	        	{
	        		previousSafeX = -1;
	        		previousSafeY = -1;
	        		goTo(pos);
	        	}
		        else
		        {
		        	pos = safeNeighbour();
		        	if ((previousSafeX == pos[0]) &&		        			
		        	    (previousSafeY == pos[1]))
		        	{
		        		safe = false;
		        		System.out.println("Safe Turned OFF");
		        		previousSafeX = -1;
		        		previousSafeY = -1;
		        	}
		        	else
		        	{
			        	previousSafeX = x;
			        	previousSafeY = y;
			        	goTo(pos);
		        	}
		        }
	        }
	    }
		
		public String bestRotation(String start, String goal)
		{
			String L, R;
	 		L = start;
	 		R = start;
	 		
	 		int contR = 0;
	 		int contL = 0;
	 		
	 		while (!L.equals(goal))
	 		{
	 			L = rotate(L, "Left");
	 			contL++;
	 		}
	 		
	 		while (!R.equals(goal))
	 		{
	 			R = rotate(R, "Right");
	 			contR++;
	 		}
	 		
	 		if (id == 0)
	 		{
	 			if (contL < contR)
	 				return "Left";
	 			
	 			return "Right";
	 		}
	 		else
	 		{
	 			if (contR < contL)
	 				return "Right";
	 			
	 			return "Left";
	 		}
		}
		
		public String rotate(String orientation, String direction)
	    {
	        switch (orientation)
	        {
	            case "Up":
	                        return (direction.equals("Right"))? "Right" : "Left";
	            case "Down":
	            			return (direction.equals("Right"))? "Left" : "Right";
	            case "Left":
	            			return (direction.equals("Right"))? "Up" : "Down";
	            case "Right":
	            			return (direction.equals("Right"))? "Down" : "Up";
	            default:
	            			return "";
	        }
	    }

		public void shoot()
		{
			arrow = false;
    		printAction("Shooting Foward");
    		
    		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.addReceiver(new AID("WorldAgent", AID.ISLOCALNAME));
			msg.setConversationId("Shoot");
			msg.setContent(x + "|" + y + "|" + orientation);
			send(msg);
		}
		
		public void grab()
	    {
	        gold = true;
	        board[y][x].setPercept(2, false);

	        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
	        // O agente cooperativo nao precisa saber que o ouro foi pego
	        // (isto eh deduzido automaticamente do momento que o ouro � encontrado)
//			msg.addReceiver(friend);
			msg.addReceiver(new AID("WorldAgent", AID.ISLOCALNAME));
			msg.setConversationId("GoldGrabbed");
    		msg.setContent(x + "|" + y);
    		send(msg);
	    }
		
		public void sense()
		{
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.addReceiver(new AID("WorldAgent", AID.ISLOCALNAME));
			msg.setConversationId("Percept");
			msg.setContent(x + "|" + y + "@" + id);
			send(msg);
		}
		
	    public void goTo(int[] coord)
	    {
	        ArrayList<Node> open = new ArrayList<>();
	        ArrayList<Node> closed = new ArrayList<>();
	        Node n = new Node (x,y,orientation);
	        
	        n.setH(coord[0],coord[1]);
	        n.setF();
	        
	        do 
	        {
	        	generator(n, open, closed, coord);
	            Collections.sort(open, new Comparator<Node>() 
	            {
	                @Override
	                public int compare(Node a, Node b) 
	                {
	                    return a.getF() -  b.getF();
	                }
	            });
	            n = open.get(0);
	            open.remove(0);	            
	        }while (n.getH() != 0);
	        
	        backtrack(n,null);
	        
	        open.clear();
	        closed.clear();
	    }
	    
	    public void generator(Node source, ArrayList<Node> open, ArrayList<Node> closed, int[] destination)
	    {
	    	int sourceX = source.getX();
	    	int sourceY = source.getY();
	    	String sourceOrientation = source.getOrientation();
	    	
	    	Node[] children = new Node[3];
	    	
	    	children[0] = new Node (sourceX, sourceY, sourceOrientation, "Left");
	    	children[1] = new Node (sourceX, sourceY, sourceOrientation, "Right");
	    	
	    	if (sourceOrientation.equals("Right") && sourceX < 3)
	    	{
	    		if (board[sourceY][sourceX + 1].isSafe() || !safe)
	    			children[2] = new Node (sourceX + 1, sourceY, sourceOrientation, "Moving Foward");
	    	}
	    	else if (sourceOrientation.equals("Left") && sourceX > 0)
	    	{
	    		if (board[sourceY][sourceX - 1].isSafe() || !safe)
	    			children[2] = new Node (sourceX - 1, sourceY, sourceOrientation, "Moving Foward");
	    	}
	    	else if (sourceOrientation.equals("Up") && sourceY > 0)
	    	{
	    		if (board[sourceY - 1][sourceX].isSafe() || !safe)
	    			children[2] = new Node (sourceX, sourceY - 1, sourceOrientation, "Moving Foward");
	    	}
	    	else if (sourceOrientation.equals("Down") && sourceY < 3)
	    	{
	    		if (board[sourceY + 1][sourceX].isSafe() || !safe)
	    			children[2] = new Node (sourceX, sourceY + 1, sourceOrientation, "Moving Foward");
	    	}
    		else 
    			children[2] = null; 
	        
	        for (int i = 0; i < 3; i++)
	        	if (children[i] != null)
	        	{
	        		children[i].setG(source.getG() + 1); // G = Custo do Pai (Profundidade)
	        		children[i].setH(destination[0], destination[1]); // H = Manhathan Distance
	        		children[i].setF(); // F = G + H
	        		children[i].setParent(source);
	        		
	        		if (!open.contains(children[i]))
	        			open.add(children[i]);
	        	}
	        
	        if (!closed.contains(source))
	        	closed.add(source);
	    }
	    
	    // Node n = No pai
	    // Node m = No filho
	    public void backtrack(Node n, Node m)
	    {
	    	if (n.getParent() != null)
	            backtrack(n.getParent(), n);
	        
	    	x = n.getX();
	        y = n.getY();
	        orientation = n.getOrientation();
	    	
	        if (m != null)
	        {
	        	printBoard();
	            printAction(m.getAction());
	        }
	        else if (gold)
	        {
	        	printBoard();
	        	if (x == 0 && y == 3)
	        		printAction("All Done");
	        }
	    }
	    
	    // Verifica se um agente pode atirar no Wumpus 
	    // (ou seja, o agente esta na mesma linha que o Wumpus)
	    public String canShoot() 
	    {
	    	if (arrow)
	    	{
	    		boolean flag = false;
				int[] pos = new int[2];
			 	
				// Verifica se o Wumpus esta no mesmo X ou Y do agente
				// (baseado no conhecimento do agente)
				// !---!---!---!---!---!---!---!---!---!---!---!---!---!---!---!---!---!---!
				// A principio o agente so atira se tiver certeza da posicao do wumpus
				// a busca nao-segura talvez nao devesse levar isso em consideracao
				// !---!---!---!---!---!---!---!---!---!---!---!---!---!---!---!---!---!---!
			 	for (int i = 0; i < 4; i++)
			 		if ((board[i][x].getWumpus() == 1)) // || (board[i][x].getWumpus() == 2))
		 			{
			 			flag = true;
			 			pos[0] = i;
			 			pos[1] = x;
			 			break;
			 		}
			 		else if ((board[y][i].getWumpus() == 1)) // || (board[y][i].getWumpus() == 2))	 			
			 		{
			 			flag = true;
			 			pos[0] = y;
			 			pos[1] = i;
			 			break;
			 		}
			 	
			 	// Caso o Wumpus esteja no mesmo X ou Y do agente
			 	if (flag)
			 	{
			 		String goal = "";
			 		
			 		// Verifica em que direcao o Wumpus se encontra
			 		// (com relacao ao agente) 
			 		pos[0] = y - pos[0];
			 		pos[1] = x - pos[1];
			 		
			 		// Como a verificacao eh na horizontal e vertical,
			 		// um dos resultados sempre sera 0
			 		if (pos[0] == 0)
			 		{
			 			if (pos[1] > 0)
			 				goal = "Left";
		 				else
		 					goal = "Right";
			 		}
			 		else
			 		{
			 			if (pos[0] > 0)
			 				goal = "Up";
		 				else
		 					goal = "Down";
			 		}
			 		
			 		// Se a orientacao equivale a orientacao do agente,
			 		// entao pode atirar
			 		if (goal.equals(orientation))
			 			return "OK";
			 		
			 		// Retorna a melhor direcao de rotacao 
			 		// (para chegar numa orientacao em que seja possivel
			 		// atirar no wumpus)
			 		return bestRotation(orientation,goal);		 		
			 	}
	    	}
		 	return "";
	    }

	    public int[] safeNeighbour()
	    {
	    	int[] coord = { x, y }; 
	        
	    	if (orientation.equals("Right"))
	    	{
	    		if (x < 3 && board[y][x+1].isSafe())
	    			coord[0]++;
		        else if (y < 3 && board[y+1][x].isSafe())
		            coord[1]++; 	
		        else if (x > 0 && board[y][x-1].isSafe())
		            coord[0]--;
		        else if (y > 0 && board[y-1][x].isSafe())
		            coord[1]--;
	    	}
	    	else if (orientation.equals("Left"))
	    	{
	    		if (x > 0 && board[y][x-1].isSafe())
	    			coord[0]--;
		        else if (y > 0 && board[y-1][x].isSafe())
		            coord[1]--; 	
		        else if (x < 3 && board[y][x+1].isSafe())
		            coord[0]++;
		        else if (y < 3 && board[y+1][x].isSafe())
		            coord[1]++;
	    	}
	    	else if (orientation.equals("Down"))
	    	{
	    		if (y < 3 && board[y+1][x].isSafe())
	    			coord[1]++;
		        else if (x > 0 && board[y][x-1].isSafe())
		            coord[0]--; 	
		        else if (y > 0 && board[y-1][x].isSafe())
		            coord[1]--;
		        else if (x < 3 && board[y][x+1].isSafe())
		            coord[0]++;
	    	}
	    	else if (orientation.equals("Up"))
	    	{
	    		if (y > 0 && board[y-1][x].isSafe())
	    			coord[1]--;
		        else if (x < 3 && board[y][x+1].isSafe())
		            coord[0]++; 	
		        else if (y < 3 && board[y+1][x].isSafe())
		            coord[1]++;
		        else if (x > 0 && board[y][x-1].isSafe())
		            coord[0]--;
	    	}
	        
	        return coord;
	    }

	    public int[] unvisitedNeighbour()
	    {
	    	int[] coord = { x, y };
	    	
	    	if (orientation.equals("Right"))
	    	{
	    		if (x < 3 && 
            		!board[y][x+1].isVisited() &&
            		(	(board[y][x+1].isSafe() && safe) ||
            			!safe))
    	        	coord[0]++;
	    		else if (y < 3 &&
	   	        		 !board[y+1][x].isVisited() &&
	   	        		 (	(board[y+1][x].isSafe() && safe) ||
	                			!safe))
	    			coord[1]++;
	    		else if (x > 0 &&
	   	        		 !board[y][x-1].isVisited() &&
	   	        		 (	(board[y][x-1].isSafe() && safe) ||
	                			!safe))
	    			coord[0]--;
	    		else if (y > 0 && 
    	        		 !board[y-1][x].isVisited() &&
    	        		 (	(board[y-1][x].isSafe() && safe) ||
    	        			!safe))
    	            coord[1]--;
    	        else
    	        {
    	        	coord[0] = -1;
    	        	coord[1] = -1;
    	        }	    		
	    	}
	    	else if (orientation.equals("Up"))
	    	{
	    		if  (y > 0 && 
   	        		 !board[y-1][x].isVisited() &&
   	        		 (	(board[y-1][x].isSafe() && safe) ||
   	        			!safe))
	    			coord[1]--;
	    		else if (x < 3 && 
	            		!board[y][x+1].isVisited() &&
	            		(	(board[y][x+1].isSafe() && safe) ||
	            			!safe))
    	        	coord[0]++;
	    		else if (y < 3 &&
	   	        		 !board[y+1][x].isVisited() &&
	   	        		 (	(board[y+1][x].isSafe() && safe) ||
	                			!safe))
	    			coord[1]++;
	    		else if (x > 0 &&
	   	        		 !board[y][x-1].isVisited() &&
	   	        		 (	(board[y][x-1].isSafe() && safe) ||
	                			!safe))
	    			coord[0]--;
	    		else
    	        {
    	        	coord[0] = -1;
    	        	coord[1] = -1;
    	        }
	    	}
	    	else if (orientation.equals("Left"))
	    	{
	    		if ( x > 0 &&
	        		 !board[y][x-1].isVisited() &&
	        		 (	(board[y][x-1].isSafe() && safe) ||
	            			!safe))
	    			coord[0]--;
	    		else if (y > 0 && 
	   	        		 !board[y-1][x].isVisited() &&
	   	        		 (	(board[y-1][x].isSafe() && safe) ||
	   	        			!safe))
	    			coord[1]--;
	    		else if (x < 3 && 
	            		!board[y][x+1].isVisited() &&
	            		(	(board[y][x+1].isSafe() && safe) ||
	            			!safe))
    	        	coord[0]++;
	    		else if (y < 3 &&
	   	        		 !board[y+1][x].isVisited() &&
	   	        		 (	(board[y+1][x].isSafe() && safe) ||
	                			!safe))
	    			coord[1]++;
	    		else
    	        {
    	        	coord[0] = -1;
    	        	coord[1] = -1;
    	        }
	    	}
	    	else if (orientation.equals("Down"))
	    	{
	    		if ( y < 3 &&
   	        		 !board[y+1][x].isVisited() &&
   	        		 (	(board[y+1][x].isSafe() && safe) ||
                			!safe))
	   	            coord[1]++;
	    		else if (x > 0 &&
	   	        		 !board[y][x-1].isVisited() &&
	   	        		 (	(board[y][x-1].isSafe() && safe) ||
	                			!safe))
	   	            coord[0]--;
	    		else if (y > 0 && 
	   	        		 !board[y-1][x].isVisited() &&
	   	        		 (	(board[y-1][x].isSafe() && safe) ||
	   	        			!safe))
	    			coord[1]--;
	    		else if (x < 3 && 
	    				!board[y][x+1].isVisited() &&
	    				(	(board[y][x+1].isSafe() && safe) ||
	    					!safe))
    	        	coord[0]++;
    	        else
    	        {
    	        	coord[0] = -1;
    	        	coord[1] = -1;
    	        }
	    	}
    	    	        
	        return coord;
	    }
	    
	    public int onEnd()
	    {
    		if (!gold)
	        	sense();
	        else
	        {
	        	int args[] = {0, 3};
	        	goTo(args);	        	
	        }
    		
	    	return 0;
	    }
	    
    }
}