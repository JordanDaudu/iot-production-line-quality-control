# Replit environment for the Smart IoT Quality Inspection System.
# Provides JDK 21, Maven and Node.js so both backend and frontend can run.
{ pkgs }: {
  deps = [
    pkgs.jdk21
    pkgs.maven
    pkgs.nodejs_20
  ];
}
