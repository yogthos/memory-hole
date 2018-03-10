package memory_hole;

import java.sql.SQLException;

public class ArrayAggregate implements org.h2.api.AggregateFunction{
    java.util.LinkedList<String> values = new java.util.LinkedList<String>();
    java.sql.Connection connection = null;

    @Override
    public void init(java.sql.Connection cnctn) throws java.sql.SQLException {
        connection = cnctn;
    }

    @Override
    public int getType(int[] ints) throws java.sql.SQLException {
       return java.sql.Types.ARRAY;
    }

    @Override
    public void add(Object o) throws java.sql.SQLException {
        if(o == null) {
            return;
        }

        if(java.io.BufferedReader.class.isInstance(o)) {
            try {
                values.add(((java.io.BufferedReader)o).readLine());
            }
            catch(java.io.IOException ex) {
            }
        }
        else {
            values.add(o.toString());
        }
    }

    @Override
    public Object getResult() throws java.sql.SQLException {
        return values;
    }
}
