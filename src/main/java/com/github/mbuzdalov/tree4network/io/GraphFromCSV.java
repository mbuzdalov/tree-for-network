package com.github.mbuzdalov.tree4network.io;

import com.github.mbuzdalov.tree4network.Graph;
import com.github.mbuzdalov.tree4network.GraphBuilder;

import java.io.*;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;

public final class GraphFromCSV {
    private GraphFromCSV() {}

    public static Graph fromInputStream(InputStream input) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(input))) {
            if (in.readLine().equals("src,dst,weight")) {
                GraphBuilder builder = new GraphBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    StringTokenizer st = new StringTokenizer(line, ",");
                    int src = Integer.parseInt(st.nextToken());
                    int dst = Integer.parseInt(st.nextToken());
                    int weight = Integer.parseInt(st.nextToken());
                    builder.addEdge(src, dst, weight);
                }
                return builder.result();
            } else {
                throw new IllegalArgumentException("The input does not contain a valid graph description");
            }
        }
    }

    public static Graph fromGZippedFile(File file) throws IOException {
        try (GZIPInputStream input = new GZIPInputStream(new FileInputStream(file))) {
            return fromInputStream(input);
        }
    }
}
