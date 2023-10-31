import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Paths;

public class QueryIndex {

    public static void main(String[] args) throws Exception {

        // Check if the required arguments were provided
        if (args.length < 1) {
            System.err.println("Usage: java -jar file.jar AnalyzerClassName");
            System.exit(1);
        }

/*        String indexDir = "src/main/resources/index";
        String queryFile = "src/main/resources/data/cran.qry";
        String saveFileTxtname = "src/main/resources/save/result.txt";*/

        // Default similarity class name
        String similarityClassName = "org.apache.lucene.search.similarities.BM25Similarity";

        // Default index and query file paths
        String indexDir = "../index";
        String queryFile ="../cran.qry";
        String saveFileTxtname ="../results";

        // Get the analyzer class name from the command line
        String analyzerClassName = args[0];

        // Get the similarity class name from the command line
        if (args.length == 2) {
            similarityClassName = args[1];
        }

        // The maximum number of results to display
        int MAX_RESULTS = 50;

        // Initialize the IndexSearcher and Analyzer
        FSDirectory indexx = FSDirectory.open(Paths.get(indexDir));
        IndexSearcher isearcher = new IndexSearcher(DirectoryReader.open(indexx));

        Analyzer analyzer = null;
        try {
            // Load the analyzer class dynamically based on the provided name
            analyzer = (Analyzer) Class.forName(analyzerClassName).newInstance();
            isearcher.setSimilarity(createSimilarityInstance(similarityClassName));
            // Use the loaded analyzer and similarity for indexing or searching
        } catch (Exception e) {
            System.err.println("Error loading or instantiating the classes: " + e.getMessage());
            System.exit(1);
        }

        // Create a QueryParser for your specific field
        QueryParser parser = new QueryParser("content", analyzer);

        // Read the queries from the file line by line
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(queryFile))) {
            String line;
            String index = null;
            StringBuilder content = new StringBuilder();
            int count = 0;

            //Save results in a txt file
            FileWriter writer = new FileWriter(saveFileTxtname);
            while ((line = bufferedReader.readLine()) != null) {

                // If the line starts with ".I", then it is the start of a new query
                if (line.startsWith(".I")) {
                    count++;

                    if (index != null) {
                        String queryString = content.toString().trim(); // trim leading and trailing whitespace from the query
                        if (queryString.contains("?")) { // remove the question mark if it exists
                            queryString = queryString.replace("?", "");
                        }

                        // Parse the query and search for documents
                        Query query = parser.parse(queryString);
                        ScoreDoc[] hits = isearcher.search(query, MAX_RESULTS).scoreDocs; // Get the set of results from the searcher

                        int rank = 0;
                        for (int i = 0; i < hits.length; i++) {
                            Document hitDoc = isearcher.doc(hits[i].doc); // Get the document from the set of results
                            String docId = hitDoc.get("index-ID"); // Get the document ID

                            // Increment the rank
                            rank++;

                            // Write the results to the file
                            writer.write(index
                                    + " " + "QO"
                                    + " " + docId
                                    + " " + rank
                                    + " " + hits[i].score
                                    + " " + "STANDARD" + "\n");


                        }

                        // Reset the index and content
                        content = new StringBuilder(); // reset the content
                    }
                    // Get the index of the query
                    index = String.valueOf(count);
                    //index = String.valueOf(Integer.parseInt(line.substring(3)));// get the index
                } else if (line.startsWith(".W")) {// Skip the ".W" line
                } else {
                    // Append the content lines
                    content.append(line).append(" ");
                }
            }

            // Process the last query if there is one
            if (index != null) {
                String queryString = content.toString().trim();
                if (queryString.contains("?")) {
                    queryString = queryString.replace("?", "");
                }

                // Parse the query and search for documents
                Query query = parser.parse(queryString);
                ScoreDoc[] hits = isearcher.search(query, MAX_RESULTS).scoreDocs;

                int rank = 0;
                for (int i = 0; i < hits.length; i++) {
                    Document hitDoc = isearcher.doc(hits[i].doc);
                    String docId = hitDoc.get("index-ID");
                    rank++;

                    writer.write(index
                            + " " + "QO"
                            + " " + docId
                            + " " + rank
                            + " " + hits[i].score
                            + " " + "STANDARD" + "\n");
                }
            }
            writer.close();

        }
    }

    /**
     * Create an instance of the similarity class
     * @param similarityClassName The name of the similarity class
     * @return An instance of the similarity class
     * @throws Exception If the class cannot be found or instantiated
     */
    private static Similarity createSimilarityInstance(String similarityClassName) throws Exception {
        Class<?> similarityClass = Class.forName(similarityClassName);
        return (Similarity) similarityClass.newInstance();
    }
}