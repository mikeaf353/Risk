package pas.risk.rewards;


import java.util.Set;

// SYSTEM IMPORTS
import edu.bu.jmat.Pair;

import edu.bu.pas.risk.GameView;
import edu.bu.pas.risk.TerritoryOwnerView;
import edu.bu.pas.risk.action.Action;
import edu.bu.pas.risk.action.AttackAction;
import edu.bu.pas.risk.action.FortifyAction;
import edu.bu.pas.risk.action.RedeemCardsAction;
import edu.bu.pas.risk.agent.rewards.RewardFunction;
import edu.bu.pas.risk.agent.rewards.RewardType;
import edu.bu.pas.risk.territory.Territory;
import edu.bu.pas.risk.territory.TerritoryCard;
import edu.bu.pas.risk.util.Registry;



// JAVA PROJECT IMPORTS


/**
 * <p>Represents a function which punishes/pleasures your model according to how well the {@link Action}s its been
 * choosing have been. Your reward function could calculate R(s), R(s,a), or (R,s,a'): whichever is easiest for you to
 * think about (for instance does it make more sense to you to evaluate behavior when you see a state, the action you
 * took in that state, and how that action resolved? If so you want to pick R(s,a,s')).
 *
 * <p>By default this is configured to calculate R(s). If you want to change this you need to change the
 * {@link RewardType} enum in the constructor *and* you need to implement the corresponding method. Refer to
 * {@link RewardFunction} and {@link RewardType} for more details.
 */
public class MyActionRewardFunction
    extends RewardFunction<Action>
{

    public MyActionRewardFunction(final int agentId)
    {
        super(RewardType.STATE, agentId); // change this enum if you don't want to do R(s)
    }

    public double getLowerBound() { return 0.0; }
    public double getUpperBound() { return 100.0; }

    public boolean hasEnemy(final GameView state, final Territory action){
    Set<Territory> neighbors = action.adjacentTerritories(); //get neighbors
    Registry<TerritoryOwnerView> owners = state.getTerritoryOwners();

    for(Territory n : neighbors){
        TerritoryOwnerView nov = owners.getById(n.id());
        if(nov.getOwner() != this.getAgentId()){
            return true;
        }
    }


        return false;
    }

    /** {@inheritDoc} */
    public double getStateReward(final GameView state) { return 10.0; } // this sucks you'll need to change this

    /** {@inheritDoc} */
    public double getHalfTransitionReward(final GameView state,
                                          final Action action) 
        { 
        double reward = 0.0;
        if(action instanceof AttackAction){
            // 1. reward having an advantage over enemy
            // 2. punish for attacking with less than 4
        

            AttackAction a = (AttackAction) action;
            Territory f = a.from();
            Territory t = a.to();
            Registry<TerritoryOwnerView> owners = state.getTerritoryOwners();
            TerritoryOwnerView fov = owners.getById(f.id());
            TerritoryOwnerView tov = owners.getById(t.id());
            double myArmies = fov.getArmies();
            double enemies = tov.getArmies();

            if(myArmies / enemies >= 1.5){
                reward += 10;
            }

            if(myArmies < 4){
                reward -= 5;
            }
            
        }
        else if(action instanceof FortifyAction){
            FortifyAction fo = (FortifyAction) action;
            Territory f = fo.from();
            Territory t = fo.to();
            Registry<TerritoryOwnerView> owners = state.getTerritoryOwners();
            TerritoryOwnerView fov = owners.getById(f.id());
            TerritoryOwnerView tov = owners.getById(t.id());
            int deltaArmies = fo.deltaArmies();
            
            if(hasEnemy(state, f)){
                Set<Territory> neighbors = f.adjacentTerritories();
                double forTotalEnemies = 0.0;
                double forStrongest = 0.0;

                for(Territory n : neighbors){
                    TerritoryOwnerView nov = owners.getById(n.id());
                    if(nov.getOwner() != this.getAgentId()){
                        forTotalEnemies += nov.getArmies();
                        if(nov.getArmies() > forStrongest){
                            forStrongest = nov.getArmies();
                        }
                    }
                }
                int remain = fov.getArmies() - deltaArmies;
                if(remain < forStrongest){ //punish weakening a position
                    reward -= 5;
                }
            }

            if(hasEnemy(state, t)){
                Set<Territory> neighbors = f.adjacentTerritories();
                double toTotalEnemies = 0.0;
                double toStrongest = 0.0;

                for(Territory n : neighbors){
                    TerritoryOwnerView nov = owners.getById(n.id());
                    if(nov.getOwner() != this.getAgentId()){
                        toTotalEnemies += nov.getArmies();
                        if(nov.getArmies() > toStrongest){
                            toStrongest = nov.getArmies();
                        }
                    }
                }
                int remain = tov.getArmies() + deltaArmies;
                if(remain > toStrongest){ //punish weakening a position
                    reward += 5;
                }
            }
        }
        else if(action instanceof RedeemCardsAction){
        }
        else {//NoAction instance

        }
            return reward; }

    /** {@inheritDoc} */
    public double getFullTransitionReward(final GameView state,
                                          final Action action,
                                          final GameView nextState) { return Double.NEGATIVE_INFINITY; }

}



