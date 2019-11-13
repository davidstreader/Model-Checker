package mc.compiler;

import mc.exceptions.CompilationException;
import mc.processmodels.ProcessModel;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Instantiate used to iterate over domains
 */
class Instantiate {
      /*
         domains are built once per compile  hence static
         every forall builds a new instantiate object
         peek() returns the current instantiation
         next update the indexes and then returns peek()
       */

      private static  Map<String, List<ProcessModel>> domains = new TreeMap<>(); //map Domain to list of processes

    public static boolean validateDomains(List<String> globlFreeVariables) {
        for (String varDom : globlFreeVariables) {
            String[] parts = StringUtils.split(varDom, ':');
            String dom = parts[1];
            if (!domains.containsKey(dom)) return false;
        }
        return true;
    }
    public static void buildFreshDomains(Map<String, ProcessModel> processMap) {
        //build domains name 2 process list ,using processMap
        domains = new TreeMap<>();  // must be fresh each time
        for (String k : processMap.keySet()) {
            // if (allVariables.contains(k)) continue;  //do not add variable to domain
            String[] parts = StringUtils.split(k, ':');
            String dom = "";
            if (parts.length == 0) {
                return;
            } else if (parts.length == 1) {
                dom = "*";
            } else if (parts.length > 1) {
                dom = parts[1];
            }
            if (domains.containsKey(dom)) {
                domains.get(dom).add(processMap.get(k));
            } else {
                domains.put(dom, new ArrayList<>(Arrays.asList(processMap.get(k))));
            }
        }
        //System.out.println("buildFreshDaomains "+domains2String());
    }
    //One Instantion object holds the  Global  variables
    //Another object is used to hold the Local variables in forAll
     private Map<String, Integer> indexes = new TreeMap<>();  //Map variable 2 Domain 2 index

    public static String domains2String() {
        StringBuilder sb = new StringBuilder();

        sb.append("Instantiate working on  domains ");
        domains.keySet().stream().forEach(x -> {
            sb.append("\n    " + x + "->");
            domains.get(x).stream().forEach(key -> sb.append(key.getId() + ", "));
        });
        return sb.toString();
    }

    /**
     * For  Variables with different Domains!
     * provides the next() variable instantiation and
     * end() to indicate that all instantiations given
     *
     //   * @param processMap    Conceptually processes and variables are disjoint but
     * @param freeVariables pragmatically they overlap
    //   * @param allVariables  needed to prevent domains holding variables
     * @throws CompilationException
     */
    public Instantiate( List<String> freeVariables
                       //REF  ,List<String> allVariables
    ) throws CompilationException {

        //System.out.println("*****Instantiate allVars "+allVariables+" free "+freeVariables);

        if (freeVariables.size() == 0) {
            throw new CompilationException(getClass(), "Empty Variable List");
        }
        //initialise indexes for freeVariables
        for (String var : freeVariables) {
            indexes.put(var, (Integer) 0);
        }

        //System.out.println("***Instantiate "+ PetrinetInterpreter.asString(processMap));
        //System.out.println("***inst " +asString(peek()));
        //REF     this.processMap = processMap;
        //System.out.println(" constructor "+this.myString());
    }

/*
  peek returns the current instantiation
 */
    public Map<String, ProcessModel> peek() {
        Map<String, ProcessModel> currentInstantiation = new TreeMap<>();
        //System.out.print("PEEK ");
        //System.out.println("  domains "+domains.keySet());
        for (String key : indexes.keySet()) {
            //System.out.println("key "+key);
            String[] parts = StringUtils.split(key, ':');
            String dom = parts[1];
            //System.out.println("dom "+dom);
            currentInstantiation.put(key, domains.get(dom).get(indexes.get(key)));
            //System.out.print("_"+key+"->"+currentInstantiation.get(key).getId()+", ");
        }
        //System.out.println();
        return currentInstantiation;
    }

    /**
     * at end do nothing So check for end prior to use!
     */
    public Map<String, ProcessModel> next() {
        for (String var : indexes.keySet()) {
            String[] parts = StringUtils.split(var, ':');
            String dom = parts[1];
            if (indexes.get(var) == (domains.get(dom).size() - 1)) {
                indexes.put(var, 0);
            } else {
                indexes.put(var, indexes.get(var) + 1);
                break;
            }
        }
        //System.out.println("**next " +asString(peek()));
        return peek();
    }

    public boolean end() {
        for (String var : indexes.keySet()) {
            String[] parts = StringUtils.split(var, ':');
            String dom = parts[1];
            if (indexes.get(var) != (domains.get(dom).size() - 1)) return false;
        }
        return true;
    }

    public Integer permutationCount() {
        int cnt = 1;
        for (String var : indexes.keySet()) {
            String[] parts = StringUtils.split(var, ':');
            String dom = parts[1];
            cnt = cnt * domains.get(dom).size();
        }
        return cnt;
    }

    public String myString() {
        StringBuilder sb = new StringBuilder();

  /*    sb.append("Instantiate working on  domains ");
      domains.keySet().stream().forEach(x -> {
        sb.append("\n    " + x + "->");
        domains.get(x).stream().forEach(key -> sb.append(key.getId() + ", "));
      });
      */
        sb.append(" Instantiation   indexes  are ");
        indexes.keySet().stream().forEach(x -> {
            sb.append("  " + x + "->" + indexes.get(x));

        });


        Map<String, ProcessModel> currentInstantiation = peek();
        sb.append("\n  currentInstantiation ");
        currentInstantiation.keySet().stream().forEach(x -> {
            sb.append("  " + x + "->" + currentInstantiation.get(x).getId());

        });
        return sb.toString();
    }
}

