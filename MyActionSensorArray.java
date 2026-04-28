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
import edu.bu.pas.risk.action.NoAction;
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

    public static final int NUM_FEATURES = 25;

    public MyActionSensorArray(final int agentId)
    {
        super(agentId);
    }

    // Sensor Array [action 0-3][src 4-8][dst 9-13][attack 14-17][fortify 18-20][redeem 21-24]
    // Action - attack=0, fortify=1, redeem=2, nothing=3

    public Matrix getSensorValues(final GameView state,
                                  final int actionCounter,
                                  final Action action)
    {

        Matrix fin = Matrix.full(1, NUM_FEATURES, 0);

        Territory t = null;
        Territory f = null;
        if(action instanceof NoAction) {
            fin.set(0, 3, 1.0);
            return fin;

        } else if(action instanceof FortifyAction) {
            FortifyAction fa = (FortifyAction) action;
            t = fa.to();
            f = fa.from();

        } else if(action instanceof AttackAction) {
            AttackAction aa = (AttackAction) action;
            t = aa.to();
            f = aa.from();

        } else if(action instanceof RedeemCardsAction) {
            RedeemCardsAction ra = (RedeemCardsAction) action;
            int tradeInNum = 1 +  state.getNumPreviousRedemptions();
            int armiesRedeemed = TerritoryCard.getRedemptionAmount(tradeInNum);

            //action
            fin.set(0, 2, 1.0);
            //redeem
            fin.set(0, 21, armiesRedeemed);
            fin.set(0, 22, ra.card1().armyValue());
            fin.set(0, 23, ra.card2().armyValue());
            fin.set(0, 24, ra.card3().armyValue());
            
            return fin;
        }

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


            if( stotale != 0) {
                str = sourcearmy/stotale;
            }
            if( sso != 0) {
                ssr = sourcearmy/sso;
            }
            if( ttotale != 0) {
                ttr = targetarmy/ttotale;
            }
            if( tso != 0) {
                tsr = targetarmy/tso;
            }
            
            //src
            fin.set(0, 4, sourcearmy);
            fin.set(0, 5, stotale);
            fin.set(0, 6, str);
            fin.set(0, 7, sso);
            fin.set(0, 8, ssr);
            //dst
            fin.set(0, 9, targetarmy);
            fin.set(0, 10, ttotale);
            fin.set(0, 11, ttr);
            fin.set(0, 12, tso);
            fin.set(0, 13, tsr);


        if(action instanceof AttackAction){
            //1. My armies 2.Enemy Armies 3. attack ratio 4.TotalBadNeighbors 5.totalthreatratio 
            //6.strongest negihbor ratio 7.strongest negithbor army
            double aratio = 0;
            double totalthreat = 0;
            double threeDice = 0;
            double armyDif = sourcearmy - targetarmy;

            if(targetarmy != 0) {
                aratio = sourcearmy/targetarmy;
            }
            if(ttotale != 0) {
                totalthreat = sourcearmy/ttotale;
            }
            if(sourcearmy >= 4) {
                threeDice = 1.0;
            }

            //action
            fin.set(0, 0, 1.0);
            //attack
            fin.set(0, 14, aratio);
            fin.set(0, 15, totalthreat);
            fin.set(0, 16, threeDice);
            fin.set(0, 17, armyDif);
        }
        else if(action instanceof FortifyAction){
            FortifyAction fa = (FortifyAction) action;
            int delta = fa.deltaArmies();
            double sourceDelta = sourcearmy - delta;
            double targetDelta = targetarmy + delta;

            //action
            fin.set(0, 1, 1.0);
            //fortify
            fin.set(0, 18, delta);
            fin.set(0, 19, sourceDelta);
            fin.set(0, 20, targetDelta);
            

        }

        return fin; // row vector
    }

}
