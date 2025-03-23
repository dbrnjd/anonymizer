# anonymizer
Health Data anonymizer in java with regex pattern
Assumptions
Input Data Format:

The application assumes the input data is provided in plain text format (.txt files). No support is currently available for other file formats such as JSON, CSV, or XML.

The text data comprises unstructured, narrative-style patient notes typically authored by doctors, rather than highly structured data like Electronic Health Records (EHR).

Focus on Key Personally Identifiable Information (PII):

The application is designed to identify and anonymise a specific set of PII, including:

Names with titles (e.g., "Dr. John Smith").

Addresses, including street names, cities, and states (e.g., "123 Maple Street, Chicago, IL").

Ages and related patterns (e.g., "55-year-old" or "aged 55").

Standalone capitalized names within the text (e.g., "Sarah").

Environment Compatibility:

The solution is designed to function effectively on both local machines and cloud platforms. It ensures compatibility for smaller-scale local testing and scalability for processing larger datasets in cloud environments.

Considerations
Efficiency and Scalability:

The application optimizes resource utilization by employing single-pass file processing, precompiled regex patterns, and streaming file reads and writes to handle large datasets efficiently.

While designed for local machines, the application is flexible enough to be adapted for cloud environments with minimal adjustments.

Regex-Driven Approach:

A regex-based approach was chosen due to its accuracy in detecting structured patterns like names, addresses, and dates. Modular regex patterns ensure flexibility for detecting variations in PII formats.

Handling Unstructured Text:

The application assumes that patient notes may contain diverse sentence structures, requiring precise regex patterns to manage edge cases (e.g., multi-word cities or addresses with unit numbers like "Suite 101").

Exclusion of Non-Relevant Patterns:

Titles such as "Dr," "Mr," and "Mrs" are explicitly excluded as standalone anonymised entries. This ensures meaningful data redaction without overanonymisation.

Privacy and Data Utility:

The application balances data utility and privacy by anonymising sensitive data while retaining the narrative context for further analysis.

By making these assumptions and addressing these considerations, the application ensures efficient, scalable, and precise anonymisation of narrative patient notes while maintaining compatibility across different processing environments.
