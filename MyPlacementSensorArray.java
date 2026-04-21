package pas.risk.senses;


// SYSTEM IMPORTS
import edu.bu.jmat.Matrix;

import edu.bu.pas.risk.util.Registry;
import edu.bu.pas.risk.GameView;
import edu.bu.pas.risk.TerritoryOwnerView;
import edu.bu.pas.risk.agent.senses.PlacementSensorArray;
import edu.bu.pas.risk.territory.Territory;


// JAVA PROJECT IMPORTS
import java.util.Set;


/**
 * A suite of sensors to convert a {@link Territory} into a feature vector (must be a row-vector)
 */ 
public class MyPlacementSensorArray
    extends PlacementSensorArray
{

    public static final int NUM_FEATURES = 5;

    public MyPlacementSensorArray(final int agentId)
    {
        super(agentId);
    }

    public Matrix getSensorValues(final GameView state,
                                  final int numRemainingArmies,
                                  final Territory territory)
    {
        Matrix fin = Matrix.full(1, NUM_FEATURES, 0);
        //get adjacent territories
        Set<Territory> neighbors = territory.adjacentTerritories();
        Registry<TerritoryOwnerView> owners = state.getTerritoryOwners();

        double totalarmies = 0;

        double totalenemies = 0;

        double sdr = 0;
        double strongest = -1;

        double tdr = 0;
        for (Territory t : neighbors){
            for(TerritoryOwnerView o : owners){
                if(o.id() == territory.id()){
                    totalarmies = o.getArmies();
                }
                if(o.id() == t.id()){ //found the territory.
                    //1. Cur armies 2. total enemys 3. strongest defence ratio 4. total defence ratio
                    if(o.getOwner() != this.getAgentId()){
                        totalenemies += o.getArmies();
                        if(o.getArmies() < strongest){
                            strongest = o.getArmies();
                        }
                    }
                }
            }
        }
        sdr = strongest/totalenemies;
        tdr = totalarmies/totalenemies;

        fin.set(0, 0, totalarmies);
        fin.set(0, 1, totalenemies);
        fin.set(0, 2, sdr);
        fin.set(0, 3, tdr);


        return fin; // row vector
    }

}

