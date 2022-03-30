package ed.inf.adbs.minibase;

import ed.inf.adbs.minibase.base.*;
import ed.inf.adbs.minibase.operator.*;
import ed.inf.adbs.minibase.parser.QueryParser;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * In-memory database system
 *
 */
public class Minibase {

    public static void main(String[] args) {

        if (args.length != 3) {
            System.err.println("Usage: Minibase database_dir input_file output_file");
            return;
        }

        String databaseDir = args[0];
        String inputFile = args[1];
        String outputFile = args[2];

        evaluateCQ(databaseDir, inputFile, outputFile);

//        parsingExample(inputFile);
    }

    public static void evaluateCQ(String databaseDir, String inputFile, String outputFile) {
        try {
            System.out.println("Start Minibase query evaluation:");
            DBCatalog dbc = DBCatalog.getInstance();
            dbc.init(databaseDir);

            Query query = QueryParser.parse(Paths.get(inputFile));
            System.out.println("Input query: " + query);

            Operator queryPlan = buildQueryPlan(query);
            if (queryPlan != null)
                queryPlan.dump();
            else
                System.out.println("-- Empty query --");

        } catch (Exception e) {
            System.err.println("Exception occurred during parsing");
            e.printStackTrace();
        }
    }

    private static Operator buildQueryPlan(Query query) {
        List<RelationalAtom> relationalAtoms = new ArrayList<>();
        List<ComparisonAtom> selectConditions = new ArrayList<>();

        // Get the list of appeared variable names
        List<String> usedVariables = new ArrayList<>();
        for (Atom atom : query.getBody()) {
            // the variables in ComparisonAtom are permitted to appear in some RelationalAtom
            // so no need to check the variables in ComparisonAtom
            if (atom instanceof RelationalAtom) {
                for (Term term : ((RelationalAtom) atom).getTerms()) {
                    if (term instanceof Variable && !usedVariables.contains(((Variable) term).getName()))
                        usedVariables.add(((Variable) term).getName());
                }
            }
        }

        // Split the body atoms into two groups: RelationalAtom and ComparisonAtom
        // Replace the constants in RelationalAtom with new variable, and add a corresponding ComparisonAtom
        for (Atom atom : query.getBody()) {
            if (atom instanceof RelationalAtom) { // Handle the relationalAtom
                // replace the constant in RelationalAtom with new variable, adding new ComparisonAtom correspondingly
                List<Term> termList = ((RelationalAtom) atom).getTerms();
                String relationName = ((RelationalAtom) atom).getName();
                for (int i = 0; i < termList.size(); i++) {
                    Term originalTerm = termList.get(i);
                    if (originalTerm instanceof Constant) {
                        String newVarName = generateNewVariableName(usedVariables);
                        termList.set(i, new Variable(newVarName));
                        selectConditions.add(new ComparisonAtom(
                                new Variable(newVarName),
                                originalTerm,
                                ComparisonOperator.fromString("=")
                        ));
                    }
                }
                relationalAtoms.add(new RelationalAtom(relationName, termList));
            } else { // Handle the ComparisonAtom
                // store the select-condition atoms into a list
                // identify the join conditions and store them into // together with the involved relations
                selectConditions.add((ComparisonAtom)atom);
            }
        }

        // Generate the query plan tree
        Operator root = null;
        List<String> previousVariables = new ArrayList<>();
        for (RelationalAtom rAtom : relationalAtoms) {
            // this variable list will be used to identify which comparisonAtom should be applied on this relationalAtom
            List<String> subtreeVariables = new ArrayList<>();
            for (Term term : rAtom.getTerms()) {
                if (term instanceof Variable) subtreeVariables.add(((Variable) term).getName());
            }

            // Scan operation
            Operator subtree = new ScanOperator(rAtom);

            // Select operation
            List<ComparisonAtom> selectCompAtomList = new ArrayList<>();
            for (ComparisonAtom cAtom : selectConditions)
                if (variableAllAppeared(cAtom, subtreeVariables))
                    selectCompAtomList.add(cAtom);
            subtree = new SelectOperator(subtree, selectCompAtomList);

            // Join operation
            List<String> mergedVariables = new ArrayList<>();
            mergedVariables.addAll(previousVariables);
            mergedVariables.addAll(subtreeVariables);
            if (root == null) {
                root = subtree;
            } else {
                List<ComparisonAtom> joinCompAtomList = new ArrayList<>();
                for (ComparisonAtom cAtom : selectConditions) {
                    if (!variableAllAppeared(cAtom, previousVariables) &&
                            !variableAllAppeared(cAtom, subtreeVariables) &&
                            variableAllAppeared(cAtom, mergedVariables))
                        joinCompAtomList.add(cAtom);
                }
                root = new JoinOperator(root, subtree, joinCompAtomList);
            }

            // update variable list after two subtrees are joined
            previousVariables = mergedVariables;
        }

        root.dump();
        root.reset();
        System.out.println("--------Project--------");

        // Project operation & Aggregation operations
        List<Term> headTerms = new ArrayList<>(query.getHead().getTerms());
        Term lastHeadTerm = headTerms.get(headTerms.size() - 1);
        if (lastHeadTerm instanceof AggTerm) {
            // contain aggregation operation
//            String aggVar = ((AggTerm) lastHeadTerm).getVariable();
//            headTerms.set(headTerms.size() - 1, new Variable(aggVar));
//            root = new ProjectOperator(root, new RelationalAtom(query.getHead().getName(), headTerms));
//            root.dump();
//            root.reset();
            System.out.println("------AGG-------");
            if (lastHeadTerm instanceof Sum) {
//                root = new SumOperator(root, (Sum)lastHeadTerm);
                root = new SumOperator(root, query.getHead());
            } else {
//                root = new SingleAvgOperator(root, (Avg)lastHeadTerm);
                root = new AvgOperator(root, query.getHead());
            }
        } else {
            // not contain aggregation operation, project directly
            root = new ProjectOperator(root, query.getHead());
        }
        return root;
    }

    private static String generateNewVariableName(List<String> usedNames) {
        int count = 0;
        String newVar = "var" + String.valueOf(count);
        while (usedNames.contains(newVar)) {
            count++;
            newVar = "var" + String.valueOf(count);
        }
        usedNames.add(newVar);
        return newVar;
    }

    /**
     * Check whether the variables in a Comparison all appeared in the variable list of a sub-tree.
     * @param comparisonAtom
     * @param currentVariables
     * @return
     */
    private static boolean variableAllAppeared(ComparisonAtom comparisonAtom, List<String> currentVariables) {
        if (comparisonAtom.getTerm1() instanceof Variable)
            if (!currentVariables.contains(((Variable) comparisonAtom.getTerm1()).getName()))
                return false;
        if (comparisonAtom.getTerm2() instanceof Variable)
            if (!currentVariables.contains(((Variable) comparisonAtom.getTerm2()).getName()))
                return false;
        return true;
    }



    /**
     * Example method for getting started with the parser.
     * Reads CQ from a file and prints it to screen, then extracts Head and Body
     * from the query and prints them to screen.
     */

    public static void parsingExample(String filename) {
        try {
            Query query = QueryParser.parse(Paths.get(filename));
//            Query query = QueryParser.parse("Q(x, y) :- R(x, z), S(y, z, w), z < w");
//            Query query = QueryParser.parse("Q(x, w) :- R(x, 'z'), S(4, z, w), 4 < 'test string' ");

            System.out.println("Entire query: " + query);
            RelationalAtom head = query.getHead();
            System.out.println("Head: " + head);
            List<Atom> body = query.getBody();
            System.out.println("Body: " + body);
        }
        catch (Exception e)
        {
            System.err.println("Exception occurred during parsing");
            e.printStackTrace();
        }
    }

}
