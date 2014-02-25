package behaviour;

import agent.WorldAgent;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

@SuppressWarnings("serial")
public class WorldInformBehaviour extends CyclicBehaviour {

	private WorldAgent agent;
	
	public WorldInformBehaviour(Agent a)
	{
		this.myAgent = a;
		agent = (WorldAgent) a;
	}
	
	public void action() 
	{
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
		ACLMessage msg = agent.receive(mt);
		
		while (msg != null)
		{
			if (msg.getConversationId().equals("Percept"))
			{
				// Determina onde se encontra a posi��o X do agente (contida na mensagem)
				String content = msg.getContent();
				int begin = 0;
				int end = content.indexOf('|');
				
				// Obtem e transforma a string contendo a posi��o X do agente em inteiro
				int x = Integer.parseInt(content.substring(begin , end));

				// Determina onde se encontra a posi��o Y do agente (contida na mensagem)					
				begin = end + 1;
				end = content.indexOf('@', begin);

				// Obtem e transforma a string contendo a posi��o X do agente em inteiro
				int y = Integer.parseInt(content.substring(begin, end));
				
				// Determina onde se encontra o id do agente, obtem-no e o transforma para inteiro					
				begin = end + 1;
				int agent_src = Integer.parseInt(content.substring(begin));
				
				System.out.println("\nPedido de informacao do agente " + Integer.toString(agent_src) + ":");
				System.out.println("X: " + (x) + " Y: " + (y) + " Conteudo: " + (agent.getBoard()[y][x])); 

				// Cria uma mensagem de resposta, setando o performativo correto alem de
				// setar o conteudo (correspondente a informacao contida no tabuleiro)
				ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.INFORM);
				reply.setContent(agent.getBoard()[y][x]);
				
				agent.send(reply);
			}
			else if (msg.getConversationId().equals("Shoot"))
			{
				String content = msg.getContent();
				int begin = 0;
				int end = content.indexOf('|');
				
				int x = Integer.parseInt(content.substring(begin , end));
				
				begin = end + 1;
				end = content.indexOf('|', begin);
				
				int y = Integer.parseInt(content.substring(begin, end));
				
				begin = end + 1;
				String orientation = content.substring(begin);

				ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.INFORM);
				
				if (killedWumpus(x, y, orientation))
					reply.setContent("True");
				else
					reply.setContent("False");
				
				agent.send(reply);
			}
			else if (msg.getConversationId().equals("Gold Grabbed"))
			{
				String content = msg.getContent();
				int begin = 0;
				int end = content.indexOf('|');
				
				int x = Integer.parseInt(content.substring(begin , end));
				
				begin = end + 1;
				end = content.indexOf('|', begin);
				
				int y = Integer.parseInt(content.substring(begin, end));
				
				agent.setBoard(agent.getBoard()[y][x].replace("G", ""), y, x);
			}
			msg = agent.receive();
		}
	}

	// Verifica se o Wumpus foi morto pelo Agente
	public boolean killedWumpus(int x, int y, String orientation)
    {
		int i;
		String[][] board = agent.getBoard();
		
		switch (orientation)
        {
            case "Up":
                        i = y;
                        while (i > 0)
                        {    
                            if (board[i][x].contains("W"))
                            {
                            	agent.setBoard(board[i][x].replace("W", ""), i, x);
                                return true;
                            }
                            i--;
                        }
                        break;
            case "Down":
                        i = y;
                        while (i < 4)
                        {
                            if (board[i][x].contains("W"))
                            {
                            	agent.setBoard(board[i][x].replace("W", ""), i, x);
                                return true;
                            }
                            i++;
                        }
                        break;
            case "Left":
                        i = x;
                        while (i > 0)
                        {
                        	if (board[y][i].contains("W"))
                        	{
                        		agent.setBoard(board[y][i].replace("W", ""), y, i);
                                return true;
                        	}
                            i--;
                        }
                        break;
            case "Right":
                        i = x;
                        while (i < 4)
                        {
                        	if (board[y][i].contains("W"))
                        	{
                        		agent.setBoard(board[y][i].replace("W", ""), y, i);
                                return true;
                        	}
                            i++;
                        }
                        break;
    	}
		
		return false;
    }
}