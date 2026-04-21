package pas.risk.senses;


import java.util.Set;

// SYSTEM IMPORTS
import edu.bu.jmat.Matrix;

import edu.bu.pas.risk.GameView;
import edu.bu.pas.risk.TerritoryOwnerView;
import edu.bu.pas.risk.action.Action;
import edu.bu.pas.risk.action.AttackAction;
import edu.bu.pas.risk.action.FortifyAction;
import edu.bu.pas.risk.action.RedeemCardsAction;
import edu.bu.pas.risk.territory.Territory;
import edu.bu.pas.risk.util.Registry;
import edu.bu.pas.risk.agent.senses.ActionSensorArray;
import edu.bu.pas.risk.territory.TerritoryCard;




// JAVA PROJECT IMPORTS
import java.util.List;


/**
 * A suite of sensors to convert a {@link Action} into a feature vector (must be a row-vector)
 */ 
public class MyActionSensorArray
    extends ActionSensorArray
{

    public static final int NUM_FEATURES = 10;

    public MyActionSensorArray(final int agentId)
    {
        super(agentId);
    }

    public Matrix getSensorValues(final GameView state,
                                  final int actionCounter,
                                  final Action action)
    {

        Matrix fin = Matrix.full(1, NUM_FEATURES, 0);
        if(action instanceof AttackAction){
            //1. My armies 2.Enemy Armies 3. attack ratio 4.TotalBadNeighbors 5.totalthreatratio 
            //6.strongest negihbor ratio 7.strongest negithbor army  
            AttackAction a = (AttackAction) action;
            Territory t = a.to(); //get the territory thats to be attacked
            Territory f = a.from();
            Registry<TerritoryOwnerView> owners = state.getTerritoryOwners();
            TerritoryOwnerView tov = owners.getById(t.id());
            TerritoryOwnerView fov = owners.getById(f.id());
            double aarmies = fov.getArmies(); //my armies
            double ddarmies = tov.getArmies(); // enemy armies
            double aratio = 0;
            double totalenemies = 0;
            double totalthreat = 0;

            double strongestratio = 0;
            double strongest = -1;


            Set<Territory> neighbors = t.adjacentTerritories();
            for(Territory n : neighbors){
                TerritoryOwnerView nov = owners.getById(n.id());
                if(nov.getOwner() != this.getAgentId()){
                    totalenemies += nov.getArmies();
                    if(nov.getArmies() > strongest){
                        strongest = nov.getArmies();
                    }
                }
            }

            aratio = aarmies/ddarmies;
            totalthreat = aarmies/totalenemies;

            strongestratio = aarmies/strongest;

            fin.set(0, 0, aarmies);
            fin.set(0, 1, ddarmies);
            fin.set(0, 2, aratio);
            fin.set(0, 3, totalenemies);
            fin.set(0, 4, totalthreat);
            fin.set(0, 5, strongestratio);
            fin.set(0, 6, strongest);
        }
        else if(action instanceof FortifyAction){
            //1.source armies  2.total enemies 3.source threat ratio 4.source strongest opp 5.sources strongest ratio
            //6.targetarmies  7.target total enemies 8. target threat ratio 9.target strongest opp 10.target strongest ratio 
        
            double sourcearmy = 0;
            double stotale = 0;
            double str = 0;
            double sso = 0;
            double ssr = 0;

            double targetarmy = 0;
            double ttotale = 0;
            double ttr = 0;
            double tso = 0;
            double tsr = 0;



            
            FortifyAction a = (FortifyAction) action;
            Territory t = a.to(); //get the territory thats to be attacked
            Territory f = a.from();
            Registry<TerritoryOwnerView> owners = state.getTerritoryOwners();
            TerritoryOwnerView tov = owners.getById(t.id());
            TerritoryOwnerView sov = owners.getById(f.id());
            Set<Territory> targetneighbors = t.adjacentTerritories();
            Set<Territory> sourceneighbors = f.adjacentTerritories();

            
            sourcearmy = sov.getArmies();
            for(Territory sn : sourceneighbors){
                TerritoryOwnerView nov = owners.getById(sn.id());
                if(nov.getOwner() != this.getAgentId()){
                    stotale += nov.getArmies();
                    if(nov.getArmies() > sso){
                        sso = nov.getArmies();
                    }
                }
            }

            targetarmy = tov.getArmies();
            for(Territory tn : targetneighbors){
                TerritoryOwnerView nov = owners.getById(tn.id());
                if(nov.getOwner() != this.getAgentId()){
                    ttotale += nov.getArmies();
                    if(nov.getArmies() > tso){
                        tso = nov.getArmies();
                    }
                }
            }
            str = sourcearmy/stotale;
            ssr = sourcearmy/sso;

            ttr = targetarmy/ttotale;
            tsr = targetarmy/tso;
            
            fin.set(0, 0, sourcearmy);
            fin.set(0, 1, stotale);
            fin.set(0, 2, str);
            fin.set(0, 3, sso);
            fin.set(0, 4, ssr);
            fin.set(0, 5, targetarmy);
            fin.set(0, 6, ttotale);
            fin.set(0, 7, ttr);
            fin.set(0, 8, tso);
            fin.set(0, 9, tsr);

        }
        else if(action instanceof RedeemCardsAction){
            RedeemCardsAction a = (RedeemCardsAction) action;
            int tradeInNum = 1 +  state.getNumPreviousRedemptions();
            int armiesRedeemed = TerritoryCard.getRedemptionAmount(tradeInNum);
            fin.set(0, 0, armiesRedeemed);
            fin.set(0, 1, a.card1().armyValue());
            fin.set(0, 2, a.card2().armyValue());
            fin.set(0, 3, a.card3().armyValue());
        }
        else {//NoAction instance
            System.out.println("No action :/");
            fin.set(0, 0, 1.0); //this way the model can tell if no action comes up, this may not be relevent lol
        }


        

        return fin; // row vector
    }

}

