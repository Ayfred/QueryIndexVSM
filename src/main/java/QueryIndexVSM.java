import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.similarities.ClassicSimilarity;


import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Paths;

public class QueryIndexVSM {

    public static void main(String[] args) throws Exception {
        String indexDir = "../index";
        String queryFile ="../cran.qry";
        int MAX_RESULTS = 50;

        // Initialize the IndexSearcher and Analyzer
        FSDirectory indexx = FSDirectory.open(Paths.get(indexDir));
        IndexSearcher isearcher = new IndexSearcher(DirectoryReader.open(indexx));

        isearcher.setSimilarity(new ClassicSimilarity());

        Analyzer analyzer = new StandardAnalyzer();

        // Create a QueryParser for your specific field
        QueryParser parser = new QueryParser("content", analyzer);

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(queryFile))) {
            String line;
            String index = null;
            StringBuilder content = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {

                if (line.startsWith(".I")) {
                    if (index != null) {
                        String queryString = content.toString().trim(); // trim leading and trailing whitespace from the query
                        if (queryString.contains("?")) { // remove the question mark if it exists
                            queryString = queryString.replace("?", "");
                        }

                        System.out.println("Query: " + queryString);

                        // Parse the query and search for documents
                        Query query = parser.parse(queryString);
                        ScoreDoc[] hits = isearcher.search(query, MAX_RESULTS).scoreDocs; // Get the set of results from the searcher

                        System.out.println("Documents: " + hits.length);
                        for (int i = 0; i < hits.length; i++) {
                            Document hitDoc = isearcher.doc(hits[i].doc);
                            System.out.println(i + ") " + index + " " + hits[i].score);
                        }

                        content = new StringBuilder(); // reset the content
                    }

                    index = line.substring(3);// get the index
                } else if (line.startsWith(".W")) {// Skip the ".W" line
                    // Skip the ".W" line
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

                System.out.println("Query: " + queryString);

                // Parse the query and search for documents
                Query query = parser.parse(queryString);
                ScoreDoc[] hits = isearcher.search(query, MAX_RESULTS).scoreDocs;


                System.out.println("Documents: " + hits.length);
                for (int i = 0; i < hits.length; i++) {
                    Document hitDoc = isearcher.doc(hits[i].doc);
                    System.out.println(i + ") " + index + " " + hits[i].score);

                }

            }


        }
    }
}
