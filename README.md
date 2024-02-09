<h1>This is an annotation-driven parser to convert Java DTOs to Zod Schemas.</h1>
<ul>
<li>
  Use @GenerateZodSchema on a relevant class. 
</li>
  <li>
Use the source directory in the DtoParser class to set where the generated schemas will land.    
  </li>
<li>
The build.gradle.kts includes the task set up I use to trigger this whenever my project compiles, so any new or edited DTOs are updated.  
</li>

  
</ul>
