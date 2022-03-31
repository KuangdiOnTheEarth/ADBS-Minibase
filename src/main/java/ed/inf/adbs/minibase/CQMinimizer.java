package ed.inf.adbs.minibase;

import ed.inf.adbs.minibase.base.*;
import ed.inf.adbs.minibase.parser.QueryParser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * Minimization of conjunctive queries
 *
 */
public class CQMinimizer {

    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.println("Usage: CQMinimizer input_file output_file");
            return;
        }

        String inputFile = args[0];
        String outputFile = args[1];

        minimizeCQ(inputFile, outputFile);

//        parsingExample(inputFile);
    }

    /**
     * CQ minimization procedure
     *
     * Assume the body of the query from inputFile has no comparison atoms
     * but could potentially have constants in its relational atoms.
     * Logic of minimization is described as in-line comments
     *
     */
    public static void minimizeCQ(String inputFile, String outputFile) {
        try {
            // read and parse the input query
            Query query = QueryParser.parse(Paths.get(inputFile));
            List<Term> head = query.getHead().getTerms();
            List<Atom> body =query.getBody();
//            System.out.println("Head: " + head + "\nBody: " + body); // preview the input query

            // convert the type of output variables to Variable
            List<Variable> output_variables = new ArrayList<Variable>();
            for (Term term : head) output_variables.add((Variable) term);
            // convert the type of atoms in body to RelationalAtom
            List<RelationalAtom> body_atoms = new ArrayList<RelationalAtom>();
            for (Atom atom : body) body_atoms.add((RelationalAtom) atom);

            // try to remove Atoms
            // we just need to check the removability once for each atom
            // iterate from tail to head, so removing an atom in the front will not influence the fetch of next atom
            for (int i = body_atoms.size(); i > 0; i--) {
                int cur_atom_idx = body_atoms.size() - i;
                RelationalAtom candidate_atom = body_atoms.get(cur_atom_idx);
//                System.out.println("-- Checking " + candidate_atom);

                // check whether this Atom contains output variable that is unique in body
                if (contain_unique_output_variable(cur_atom_idx, body_atoms, output_variables)) {
                    continue; // if this atom contains unique output variable, we should not remove it
                }
                // finding query homomorphism from original query to reduced query is equivalent to
                // checking whether the current atom holds a homomorphism to any of other atoms
                if (has_homomorphism(cur_atom_idx, body_atoms)) {
//                    System.out.println("---- Atom Removed ! ! ! ! ! ! ! ! ! ! ! ! !");
                    body_atoms.remove(cur_atom_idx);
                }
            }

            // print minimal CQ to output file
//            System.out.println("Minimal: " + body_atoms);

            List<Atom> reduced_body = new ArrayList<>(body_atoms);
            Query minimal_query = new Query(query.getHead(), reduced_body);
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            writer.write(minimal_query.toString());
            writer.close();

        } catch (Exception e) {
            System.err.println("Exception occurred during CQ minimization");
            e.printStackTrace();
        }
    }

    /**
     * Check whether an atom contains some output variables that never appeared in any other atoms.
     * @param cur_atom_idx the index of the being checked atom in the atom list
     * @param body_atoms the list of atoms
     * @param output_variables the list of output variables
     * @return True if this atom contain unique output variable; False otherwise
     */
    private static boolean contain_unique_output_variable(int cur_atom_idx, List<RelationalAtom> body_atoms, List<Variable> output_variables) {
        for (Term term : body_atoms.get(cur_atom_idx).getTerms()) {
            if (!(term instanceof Variable)) continue; // whether this term appears in output
            if (output_variables.contains(term)) { // whether this term appears in other atoms
                boolean is_duplicate = false;
                for (RelationalAtom atom : body_atoms) {
                    if (body_atoms.indexOf(atom) == cur_atom_idx) continue; // compare with other atoms, ignore itself
                    if (atom.getTerms().contains(term)) {
                        is_duplicate = true;
                        break;
                    }
                }
                if (is_duplicate) {
//                    System.out.println("---- " + term + " is not unique output variable"); // this term is output variable, but not unique
                } else {
                    // this atom contains a term, which appears in the output, but no duplication in other atoms
//                    System.out.println("---- " + term + " is unique!!! --> this atom can not be removed!!!");
                    return true;
                }
            } else {
//                System.out.println("---- " + term + " is not unique output variable"); // this term is not output variable
            }

        }
        return false;
    }

    /**
     * Check whether an atom holds homomorphism to any other atoms in the given list
     * @param cur_atom_idx the index of the being checked atom in the atom list
     * @param body_atoms the list of atoms
     * @return True if this atom holds a homomorphism to another atom; False otherwise
     */
    private static boolean has_homomorphism(int cur_atom_idx, List<RelationalAtom> body_atoms) {
        RelationalAtom cur_atom = body_atoms.get(cur_atom_idx);
        for (RelationalAtom target_atom : body_atoms) {
//            System.out.println("---- Comparing with " + target_atom);
            if (body_atoms.indexOf(target_atom) == cur_atom_idx) continue; // ignore the being checked atom itself
            if (!Objects.equals(target_atom.getName(), cur_atom.getName())) continue; // ignore the atom with different relation name
            // homomorphism from cur_atom to target_atom exists if:
            // 1. the non-variable terms in the being checked atom matches the value in corresponding place of that atom
            // 2. the variables in the being checked atom not appear in any other atoms
            boolean found_homo = true;
            for (int i = 0; i < cur_atom.getTerms().size(); i++) {
                Term cur_term = cur_atom.getTerms().get(i);
                if (cur_term instanceof Constant) {
                    if (!cur_term.equals(target_atom.getTerms().get(i))) {
//                        System.out.println("------ the " + i + "th element is constant, not match");
                        found_homo = false;
                        break;
                    }
                } else { // this term is variable
                    if (!cur_term.equals(target_atom.getTerms().get(i))) {
                        // if the variable names are not the same, check whether there is a mapping
                        for (RelationalAtom temp_atom : body_atoms) {
                            if (temp_atom.getTerms().contains(cur_term)) {
                                // once we found an atom contains this term, there are 3 situations:
                                if (body_atoms.indexOf(temp_atom) == cur_atom_idx) {
                                    // ignore the current atom itself
                                    continue;
                                } else if (body_atoms.indexOf(temp_atom) == body_atoms.indexOf(target_atom)) {
                                    // in the target term, the being checked term should only appear at the corresponding place
                                    if (cur_term != target_atom.getTerms().get(i)) {
                                        found_homo = false;
//                                        System.out.println("------ the " + i +"th element has invalid variable-to-variable mapping with " + temp_atom);
                                        break;
                                    }
                                } else {
                                    // if an atom is neither the current atom nor the target atom (that we are trying to build homomorphism with),
                                    // it should not contain this variable; otherwise the mapping of this variable will change other parts of the query body,
                                    //
                                    found_homo = false;
//                                    System.out.println("------ the " + i +"th element is variable, but appeared in " + temp_atom);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if (found_homo) {
//                System.out.println("---- Found homomorphism with " + target_atom);
                return true;
            } else {
//                System.out.println("------ Not homomorphism with this atom");
            }
        }
//        System.out.println("---- No homomorphism, can not be removed");
        return false; // fails to find homomorphism to any other atoms
    }



    /**
     * Example method for getting started with the parser.
     * Reads CQ from a file and prints it to screen, then extracts Head and Body
     * from the query and prints them to screen.
     */

    public static void parsingExample(String filename) {

        try {
            Query query = QueryParser.parse(Paths.get(filename));
//            Query query = QueryParser.parse("Q(x, y) :- R(x, z), S(y, z, w)");
//            Query query = QueryParser.parse("Q() :- R(x, 'z'), S(4, z, w)");

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
