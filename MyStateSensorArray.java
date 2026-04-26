package pas.risk.senses;


// SYSTEM IMPORTS
import edu.bu.jmat.Matrix;

import edu.bu.pas.risk.GameView;
import edu.bu.pas.risk.util.Registry;
import edu.bu.pas.risk.territory.Board;
import edu.bu.pas.risk.territory.Territory;
import edu.bu.pas.risk.TerritoryOwnerView;
import edu.bu.pas.risk.agent.senses.StateSensorArray;
import edu.bu.pas.risk.agent.IAgent;



// JAVA PROJECT IMPORTS
import java.util.List;


/**
 * A suite of sensors to convert a {@link GameView} into a feature vector (must be a row-vector)
 */ 
public class MyStateSensorArray
    extends StateSensorArray
{
    public static final int NUM_FEATURES = 15;

    public MyStateSensorArray(final int agentId)
    {
        super(agentId);
    }

    public Matrix getSensorValues(final GameView state)
    {

        // first the number of opponents, starting simple we want two features per person
        //I want the number of territories and the number of armies, may expand to number of continents
        List<IAgent> agents = state.getAgents();
        
        Registry<TerritoryOwnerView> owners = state.getTerritoryOwners();
        Matrix fin = Matrix.full(1, NUM_FEATURES, 0);
        double[] info = new double[state.getNumAgents() * 3];


        int iterator = 0; //keep track of agent idxs
        for(int i = 0; i < info.length; i = i + 3){
            info[i] = agents.get(iterator).agentId(); 
            iterator++;
        }

        for (TerritoryOwnerView t : owners){ //now keep track of data belonging to each player
            for(int i = 0; i < info.length; i=i+3){
                if(info[i] == t.getOwner()){
                    info[i + 1] += t.getArmies();
                    info[i + 2] += 1;
                }

            }
        }
        double TotalTerritory = 0;
        double TotalArmy = 0;

        for(int i = 0; i < info.length; i = i + 3){
            TotalArmy += info[i+1];
            TotalTerritory += info[i+2]; 
        }
        //Now fill in the vector of values, We want to keep track of 6 things:
        //1. Our army ratio 2.Our Territory ration 3. maxopp army ratio 4.max territory ratio 5.total opp army ratio 6. total opp territory ratio
            double MaxOppArmy = -1;
            double MaxOppTerritory = -1;
            double TotalOppArmy = 0;
            double TotalOppTerritory = 0;
        for(int i = 0; i < info.length; i = i + 3){

            if(info[i] == this.getAgentId()){
                if(TotalArmy != 0) {
                    fin.set(0, 0, (info[i+1] / TotalArmy));
                } else {
                    fin.set(0, 0, 0);
                }
                if(TotalTerritory != 0 ) {
                    fin.set(0, 1, (info[i+2] / TotalTerritory));
                } else {
                    fin.set(0, 1, 0);
                }
                
            }else{
                if(MaxOppArmy < info[i+1]){
                    MaxOppArmy = info[i+1];
                }
                if(MaxOppTerritory < info[i+2]){
                    MaxOppTerritory = info[i+2];
                }
                TotalOppArmy += info[i+1];
                TotalOppTerritory += info[i+2];
            }
        }

        if(TotalArmy != 0) {
            fin.set(0, 2, MaxOppArmy/TotalArmy);
            fin.set(0, 4, TotalOppArmy/TotalArmy);
        } else {
            fin.set(0, 2, 0);
            fin.set(0, 4, 0);
        }
        if(TotalTerritory != 0 ) {
            fin.set(0, 3, MaxOppTerritory/TotalTerritory);
            fin.set(0, 5, TotalOppTerritory/TotalTerritory);
        } else {
            fin.set(0, 3, 0);
            fin.set(0, 5, 0);
        }
        
        
        return fin; // row vector

    }

}
