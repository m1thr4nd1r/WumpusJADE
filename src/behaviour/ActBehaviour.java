package behaviour;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import agent.HeroAgent;
import model.Node;
import model.Tile;

@SuppressWarnings("serial")
public class ActBehaviour extends OneShotBehaviour
{
	private Tile[][] board;
	private HeroAgent agent;
	private int x,y;
	
	public ActBehaviour(Agent a)
	{
		this.myAgent = a;
		agent = (HeroAgent) a;
		board = agent.getBoard();
	}
	
	public void action() 
	{
		x = agent.getX();
		y = agent.getY();
		
		int[] pos = new int[2];
        pos = unvisitedNeighbour();
    
        if (board[y][x].getPercept(2))
        {
            grab();
            agent.printAction("Grabbing Gold");
        }
        else 
        {
        	String action = canShoot();
        	
        	if (!action.isEmpty())
        	{
	        	if (!action.equals("OK"))
	        		agent.setOrientation(rotate(agent.getOrientation(), action));
	        	else
	        	{
	        		// Caso o agente atire, entao a mensagem e enviada ao WorldAgent
	        	    // para processamento
	        		shoot();
	        	}
        	}
        	else if (pos[0] != -1 && pos[1] != -1)
        	{
        		agent.setPreviousSafeX(-1);
        		agent.setPreviousSafeY(-1);
        		goTo(pos);
        	}
	        else
	        {
	        	pos = safeNeighbour();
	        	if ((agent.getPreviousSafeX() == pos[0]) &&		        			
	        	    (agent.getPreviousSafeY() == pos[1]))
	        	{
	        		agent.setSafe(false);
	        		System.out.println("Safe Turned OFF");
	        		agent.setPreviousSafeX(-1);
	        		agent.setPreviousSafeY(-1);	        		
	        	}
	        	else
	        	{
	        		agent.setPreviousSafeX(agent.getX());
	        		agent.setPreviousSafeY(agent.getY());	        		
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
 		
		if (contL < contR)
			return "Left";
		
		return "Right"; 		
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
		agent.setArrow(false);
		agent.printAction("Shooting Foward");
		
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.addReceiver(new AID("WorldAgent", AID.ISLOCALNAME));
		msg.setConversationId("Shoot");
		msg.setContent(x + "|" + y + "|" + agent.getOrientation());
		agent.send(msg);
	}
	
	public void grab()
    {
        agent.setGold(true);
        board[y][x].setPercept(2, false);

        agent.setBoard(board[y][x], y, x);
        
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        // O agente cooperativo nao precisa saber que o ouro foi pego
        // (isto eh deduzido automaticamente do momento que o ouro eh encontrado)
//		msg.addReceiver(friend);
		msg.addReceiver(new AID("WorldAgent", AID.ISLOCALNAME));
		msg.setConversationId("GoldGrabbed");
		msg.setContent(x + "|" + y);
		agent.send(msg);
    }
	
	public void sense()
	{
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.addReceiver(new AID("WorldAgent", AID.ISLOCALNAME));
		msg.setConversationId("Percept");
		msg.setContent(agent.getX() + "|" + agent.getY() + "@" + agent.getId());
		agent.send(msg);
	}
	
    public void goTo(int[] coord)
    {
        ArrayList<Node> open = new ArrayList<>();
        ArrayList<Node> closed = new ArrayList<>();
        Node n = new Node (x,y,agent.getOrientation());
        
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
    		if (board[sourceY][sourceX + 1].isSafe() || !agent.isSafe())
    			children[2] = new Node (sourceX + 1, sourceY, sourceOrientation, "Moving Foward");
    	}
    	else if (sourceOrientation.equals("Left") && sourceX > 0)
    	{
    		if (board[sourceY][sourceX - 1].isSafe() || !agent.isSafe())
    			children[2] = new Node (sourceX - 1, sourceY, sourceOrientation, "Moving Foward");
    	}
    	else if (sourceOrientation.equals("Up") && sourceY > 0)
    	{
    		if (board[sourceY - 1][sourceX].isSafe() || !agent.isSafe())
    			children[2] = new Node (sourceX, sourceY - 1, sourceOrientation, "Moving Foward");
    	}
    	else if (sourceOrientation.equals("Down") && sourceY < 3)
    	{
    		if (board[sourceY + 1][sourceX].isSafe() || !agent.isSafe())
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
        
    	agent.setX(n.getX());
        agent.setY(n.getY());
        agent.setOrientation(n.getOrientation());
    	
        if (m != null)
        {
        	agent.printBoard();
            agent.printAction(m.getAction());
        }
        else if (agent.getGold())
        {
        	agent.printBoard();
        	if (x == 0 && y == 3)
        		agent.printAction("All Done");
        }
    }
    
    // Verifica se um agente pode atirar no Wumpus 
    // (ou seja, o agente esta na mesma linha que o Wumpus)
    public String canShoot() 
    {
    	if (agent.getArrow())
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
		 		if (goal.equals(agent.getOrientation()))
		 			return "OK";
		 		
		 		// Retorna a melhor direcao de rotacao 
		 		// (para chegar numa orientacao em que seja possivel
		 		// atirar no wumpus)
		 		return bestRotation(agent.getOrientation(),goal);		 		
		 	}
    	}
	 	return "";
    }

    public int[] safeNeighbour()
    {
    	int[] coord = { x, y }; 
        
    	if (agent.getOrientation().equals("Right"))
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
    	else if (agent.getOrientation().equals("Left"))
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
    	else if (agent.getOrientation().equals("Down"))
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
    	else if (agent.getOrientation().equals("Up"))
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
    	
    	if (agent.getOrientation().equals("Right"))
    	{
    		if (x < 3 && 
        		!board[y][x+1].isVisited() &&
        		(	(board[y][x+1].isSafe() && agent.isSafe()) ||
        			!agent.isSafe()))
	        	coord[0]++;
    		else if (y < 3 &&
   	        		 !board[y+1][x].isVisited() &&
   	        		 (	(board[y+1][x].isSafe() && agent.isSafe()) ||
        				 !agent.isSafe()))
    			coord[1]++;
    		else if (x > 0 &&
   	        		 !board[y][x-1].isVisited() &&
   	        		 (	(board[y][x-1].isSafe() && agent.isSafe()) ||
            			!agent.isSafe()))
    			coord[0]--;
    		else if (y > 0 && 
	        		 !board[y-1][x].isVisited() &&
	        		 (	(board[y-1][x].isSafe() && agent.isSafe()) ||
	        			!agent.isSafe()))
	            coord[1]--;
	        else
	        {
	        	coord[0] = -1;
	        	coord[1] = -1;
	        }	    		
    	}
    	else if (agent.getOrientation().equals("Up"))
    	{
    		if  (y > 0 && 
	        		 !board[y-1][x].isVisited() &&
	        		 (	(board[y-1][x].isSafe() && agent.isSafe()) ||
	        			!agent.isSafe()))
    			coord[1]--;
    		else if (x < 3 && 
            		!board[y][x+1].isVisited() &&
            		(	(board[y][x+1].isSafe() && agent.isSafe()) ||
            			!agent.isSafe()))
	        	coord[0]++;
    		else if (y < 3 &&
   	        		 !board[y+1][x].isVisited() &&
   	        		 (	(board[y+1][x].isSafe() && agent.isSafe()) ||
                			!agent.isSafe()))
    			coord[1]++;
    		else if (x > 0 &&
   	        		 !board[y][x-1].isVisited() &&
   	        		 (	(board[y][x-1].isSafe() && agent.isSafe()) ||
                			!agent.isSafe()))
    			coord[0]--;
    		else
	        {
	        	coord[0] = -1;
	        	coord[1] = -1;
	        }
    	}
    	else if (agent.getOrientation().equals("Left"))
    	{
    		if ( x > 0 &&
        		 !board[y][x-1].isVisited() &&
        		 (	(board[y][x-1].isSafe() && agent.isSafe()) ||
            			!agent.isSafe()))
    			coord[0]--;
    		else if (y > 0 && 
   	        		 !board[y-1][x].isVisited() &&
   	        		 (	(board[y-1][x].isSafe() && agent.isSafe()) ||
   	        			!agent.isSafe()))
    			coord[1]--;
    		else if (x < 3 && 
            		!board[y][x+1].isVisited() &&
            		(	(board[y][x+1].isSafe() && agent.isSafe()) ||
            			!agent.isSafe()))
	        	coord[0]++;
    		else if (y < 3 &&
   	        		 !board[y+1][x].isVisited() &&
   	        		 (	(board[y+1][x].isSafe() && agent.isSafe()) ||
                			!agent.isSafe()))
    			coord[1]++;
    		else
	        {
	        	coord[0] = -1;
	        	coord[1] = -1;
	        }
    	}
    	else if (agent.getOrientation().equals("Down"))
    	{
    		if ( y < 3 &&
	        		 !board[y+1][x].isVisited() &&
	        		 (	(board[y+1][x].isSafe() && agent.isSafe()) ||
            			!agent.isSafe()))
   	            coord[1]++;
    		else if (x > 0 &&
   	        		 !board[y][x-1].isVisited() &&
   	        		 (	(board[y][x-1].isSafe() && agent.isSafe()) ||
                			!agent.isSafe()))
   	            coord[0]--;
    		else if (y > 0 && 
   	        		 !board[y-1][x].isVisited() &&
   	        		 (	(board[y-1][x].isSafe() && agent.isSafe()) ||
   	        			!agent.isSafe()))
    			coord[1]--;
    		else if (x < 3 && 
    				!board[y][x+1].isVisited() &&
    				(	(board[y][x+1].isSafe() && agent.isSafe()) ||
    					!agent.isSafe()))
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
    	if (agent.isAlive())
    	{
			if (!agent.getGold())			
				sense();
	        else
	        {
	        	int args[] = {0, 3};
	        	goTo(args);	        	
	        }
    	}
    	else
    		agent.removeBehaviour(this.parent);
    	return 0;
    }
    
}