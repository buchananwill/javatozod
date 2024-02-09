This is an annotation-driven parser to convert Java DTOs to Zod Schemas. 
Use @GenerateZodSchema on a relevant class. 
Use the source directory in the DtoParser class to set where the generated schemas will land.
The build.gradle.kts includes the task set up I use to trigger this whenever my project compiles, so any new or edited DTOs are updated.
