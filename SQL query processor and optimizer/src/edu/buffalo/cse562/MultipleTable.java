
package edu.buffalo.cse562;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;

public class MultipleTable {

    private static LinkedHashMap<Integer, List<Tuple>> outstream;
    private static HashSet<Tuple> h1;
    private static HashSet<Tuple> h2;
    private static int count;
    private final List<Table> tables;
    private static PlainSelect select;
    private LinkedHashSet<String> colnames;
    private Expression where;
    private static OperatorWhere wh;

    public MultipleTable(List<Table> tables, PlainSelect sel) {

        this.tables = tables;

        select = sel;
        h1 = new HashSet<Tuple>();
        h2 = new HashSet<Tuple>();

        outstream = new LinkedHashMap<Integer, List<Tuple>>();
        count = 0;

    }

    public LinkedHashMap<Integer, List<Tuple>> caculateMultipleFrom() {
        List<Expression> where_exp = new ArrayList<Expression>();
        ExpressionEvaluator e = new ExpressionEvaluator();
        where = select.getWhere();
        Expression exp = where;
        exp.accept(e);
        where_exp = e.getExpList();
 
        List<HashSet<Tuple>> tablesInMemory = new ArrayList<HashSet<Tuple>>();
        ArrayList<String> table_names = new ArrayList<String>();

        for (Table t : tables) {

            table_names.add(t.getWholeTableName());
            String dat_file = Main.data_dir + "/" + t.getWholeTableName()
                    + ".dat";
            if (Main.swap_dir == null) {
                tablesInMemory.add(readTable(dat_file, t));
            }
        }

        // get proper and expressions

        List<Expression> new_where_exp = new ArrayList<Expression>();
        for (int i = 0; i < where_exp.size(); i++) {
            if (i < where_exp.size() - 2) {
                new_where_exp.add(where_exp.get(i + 1));
                i++;
            } else {
                new_where_exp.add(where_exp.get(i));
            }
        }

        //

        if (Main.swap_dir != null) {

            try {
                tablesInMemory = new HashJoin().and_join(tables, new_where_exp);
                where.accept(e);
                colnames = e.getColNames();
                wh = new OperatorWhere(colnames, where);
            } catch (IOException e1) {
 
                e1.printStackTrace();
            }
        } else {// if small query

            for (Expression w : new_where_exp) {

                List<String> tab_list = new ArrayList<String>();
                ExpressionEvaluator e1 = new ExpressionEvaluator();
                w.accept(e1);
                colnames = e1.getColNames();
                for (String tab : colnames) {
                    tab_list.add(tab.split("\\.")[0]);
                }

                ArrayList<Integer> index = new ArrayList<Integer>();
                for (String t : tab_list) {

                    if (table_names.indexOf(t) != -1) {
                        index.add(table_names.indexOf(t));
                    }
                }

                ArrayList<HashSet<Tuple>> wc = new ArrayList<HashSet<Tuple>>();
                for (int i : index) {
                    wc.add(tablesInMemory.get(i));
                }

                wh = new OperatorWhere(colnames, w);

                eleminateTuples(wc);
 
                tablesInMemory.set(index.get(0), h1);
                h1 = new HashSet<Tuple>();

                if (index.size() > 1) {
                    tablesInMemory.set(index.get(1), h2);
                    h2 = new HashSet<Tuple>();
                }
            }

            where.accept(e);
            colnames = e.getColNames();
            wh = new OperatorWhere(colnames, where);
        }

        generateCartesianProduct(tablesInMemory, new ArrayList<Tuple>());
        return outstream;

    }

    public HashSet<Tuple> readTable(String dat_file, Table table) {

        HashSet<Tuple> output = new HashSet<Tuple>();
        BufferedReader br = null;
        try {
            String s;
            br = new BufferedReader(new FileReader(dat_file));
            while ((s = br.readLine()) != null) {

                Tuple tuple = new Tuple(table.getWholeTableName().toLowerCase(), s);
                output.add(tuple);
            }

            return output;

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }

        return output;
    }

 
    public void eleminateTuples(ArrayList<HashSet<Tuple>> outerlist) {

        for (Tuple t1 : outerlist.get(0)) {
            if (outerlist.size() > 1) {
                for (Tuple t2 : outerlist.get(1)) {
                    List<Tuple> tlist = new ArrayList<Tuple>();
                    tlist.add(t1);
                    tlist.add(t2);
                    if (wh.calculateCondition(tlist)) {
                        h1.add(t1);
                        h2.add(t2);
                    }
                }
            } else {
                List<Tuple> tlist = new ArrayList<Tuple>();
                tlist.add(t1);
                if (wh.calculateCondition(tlist)) {
                    h1.add(t1);
                }
            }

        }

    }

 

    public void generateCartesianProduct(List<HashSet<Tuple>> outerlist,
            List<Tuple> outputstream) {

        HashSet<Tuple> set = outerlist.get(0);

        for (Tuple t : set) {

            ArrayList<HashSet<Tuple>> new_outerlist = new ArrayList<HashSet<Tuple>>(
                    outerlist);
            new_outerlist.remove(set);

            if (outerlist.size() > 1) {

                outputstream.add(t);
                generateCartesianProduct(new_outerlist, outputstream);
                outputstream.remove(t);

            } else {
                outputstream.add(t);
                if (wh.calculateCondition(outputstream)) {
                    List<Tuple> t2 = new ArrayList<Tuple>(outputstream);
                    outstream.put(count++, t2);
                }
                outputstream.remove(t);

            }

        }

    }

}
