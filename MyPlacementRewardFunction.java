package pas.risk.rewards;


// SYSTEM IMPORTS
import edu.bu.jmat.Pair;

import edu.bu.pas.risk.GameView;
import edu.bu.pas.risk.TerritoryOwnerView;
import edu.bu.pas.risk.agent.rewards.RewardFunction;
import edu.bu.pas.risk.agent.rewards.RewardType;
import edu.bu.pas.risk.territory.Territory;
import edu.bu.pas.risk.util.Registry;


// JAVA PROJECT IMPORTS
import java.util.Set;

/**
 * <p>Represents a function which punishes/pleasures your model according to how well the {@link Territory}s its been
 * choosing to place armies have been. Your reward function could calculate R(s), R(s,a), or (R,a,s'): whichever
 * is easiest for you to think about (for instance does it make more sense to you to evaluate behavior when you see a
 * state, the action you took in that state, and how that action resolved? If so you want to pick R(s,a,s')).
 *
 * <p>By default this is configured to calculate R(s). If you want to change this you need to change the
 * {@link RewardType} enum in the constructor *and* you need to implement the corresponding method. Refer to
 * {@link RewardFunction} and {@link RewardType} for more details.
 */
public class MyPlacementRewardFunction
    extends RewardFunction<Territory>
{

    public MyPlacementRewardFunction(final int agentId)
    {
        super(RewardType.HALF_TRANSITION, agentId); // change this enum if you don't want to do R(s)
    }

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


    public double getLowerBound() { return -1.0; }
    public double getUpperBound() { return 1.0; }

    /** {@inheritDoc} */
    public double getStateReward(final GameView state) { return 10.0; } // this sucks you'll need to change this

    /** {@inheritDoc} */
    public double getHalfTransitionReward(final GameView state,
                                          final Territory action) { 
        //1. Reward defending threatened territories
        //2. Reward aiding expansion
        //3. Punish over defence
        //4. Punish defening unthreatened territories
        double reward = 0;


        Registry<TerritoryOwnerView> owners = state.getTerritoryOwners();
        TerritoryOwnerView tov = owners.getById(action.id());

        
            double totalEnemies = 0;

            
            double strongest = -1;

            double numEnemies = 0;

        Set<Territory> neighbors = action.adjacentTerritories();
        for(Territory n : neighbors){
            TerritoryOwnerView nov = owners.getById(n.id());
            if(nov.getOwner() != this.getAgentId()){
                totalEnemies += nov.getArmies();
                if(nov.getArmies() > strongest){
                    strongest = nov.getArmies();
                }
                numEnemies++;
            }
        }
        int placedArmy = tov.getArmies() + 1;

        
        if(hasEnemy(state, action)){
            if(placedArmy >= strongest){ //reward defending threatened territories
                reward += 0.6;
            }
            
            if(numEnemies != 0) {
                if(placedArmy >= totalEnemies/numEnemies){ // reward good expansion, div by 0 watch
                    reward += 0.4;
                }
            }
            
        
            if(placedArmy >= (2 * strongest)){ //punish being over defended
                reward -= 0.8;
            }

        }
        else{
            reward -= 0.4; //punish placing on a safe spot
        }


        reward -= 0.1;
        return reward; }

    /** {@inheritDoc} */
    public double getFullTransitionReward(final GameView state,
                                          final Territory action,
                                          final GameView nextState) { return Double.NEGATIVE_INFINITY; }

}
