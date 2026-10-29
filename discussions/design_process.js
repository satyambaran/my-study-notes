/*
	` Task Status:    
		Backlog, To Do, In Progress, Testing, Done
	` Task Steps:
		Feasibility, Plan, Deisgn, Build, Test, Production, Support
	` SDLC: Software Development Life Cycle
		Systematic process used for designing, developing, testing, and deploying software.		
        # Models: 
			* Waterfall Model: 
				A linear, sequential approach where each phase must be completed before moving on to the next.
                    Requirements are well-defined and unchangeable.
                    Requirements->coding->testing->deploying->Maintainance
			* Agile Model:
                A flexible and iterative approach, divided into small, manageable iterations or sprints.
			* V-Model: 
				An extension of the Waterfall Model that emphasizes verification and validation at each development stage.
			* Spiral Model: 
				All four phases keep repating spirally
                    Planning, Risk Analysis, Engineering, and Evaluation
                
            * Scrum:
                Sprints (time-boxed iterations), Sprint Planning, Daily Standups (Scrum Meetings), Sprint Review, and Sprint Retrospective.
			* DevOps Model: 
				Integrates development and operations teams to automate and improve the delivery process continuously.
                Cultural and technical approach that integrates development and operations teams to enhance collaboration, automation, and efficiency throughout the software development lifecycle.
                Focus on CI/CD
		# Phases:
            Project Planning: 
                Aligning technical goals with business priorities.
                Prototype and POC (Proof of Concept):
                    A software prototype is a working model with limited functionality.
                    POC: To validate an idea or concept's practicality.
            Requirements analysis: 
                Analyze requirements and identify the best solutions.
            Design: 
                Create the design for the software as per analysis.
                Includes:
                    Architectural design
                    User Interface Design
                    Design Reviews
                Keep in mind:
                    Design Patterns
                    Modularity
                    Scalability
                    Availability
                    Reliability
                    Security
            Coding: 
                Code as per design.
                Keep in mind:
                    Code reviews
                    CI using Version control git
                    Unit and integration tests
                    Automated testing
                    Documentation
            Testing:
                Test Planning
                Functional testing:
                    Matching business requirements
                Non-Functional Testing:
                    Security, Scalability, Reliability, usability and other non-functional stuffs
                UAT(User Acceptance testing):

                Best Practices:
                    Automation
                    Coverage
                    Performance testing
                    Bug tracking
            Deployment: 
                Deployment Planning
                Release management
                Post deployement testing
                Deployment automation

                Keep in mind:
                    Rollback plan
                    Monitoring
                    User communication:
                        About the new deployment
            Maintenance:
                Bug fixes
                Updates
                Performance Monitoring
                Tech support (in case of SaaS)
                Best Practice:
                    Documentation
                    Continuous Improvement
        
		# Stages of Deployment and Testing
            * Deployment Stages:
                & Dev/Local Environment
                    Purpose: Used for initial development
                    Activities: Code writing, unit testing
                & Staging env:
                    Purpose: Replica of the prod environment
                    Activities: System testing, user acceptance testing (UAT), performance testing.
                    Uses production-like data and infrastructure to simulate real-world conditions.
                    This mediatory environments can have multiple environments. Like:
                        ~ QA environment, CE(Concurrent Engineering) environment, StakeHolder enviroment
                & Prod env:
                    Purpose: The live environment where end-users interact with the application.
                    Activities: Final deployment, monitoring, user support.
            * Testing Stages:
                & Unit testing:
                    Verify individual component in isolation
                & Integration testing:
                    by qa
                    Test interactions between integrated modules(FE and BE)
                & System Testing:
                    by ce team
                    Validate the complete and integrated system against requirements
                & User Acceptance Testing (UAT):
                    Ensure the software meets user needs and business requirements.
                & Performance Testing:
                    JMeter, LoadRunner
                    Test under various load conditions and identify bottlenecks
                    Load Testing: 
                        Simulate high user loads to identify performance limits and scalability issues.
                    Stress Testing: 
                        Test the application under extreme conditions to find the breaking point and ensure it handles overload gracefully.
                    Regression Testing:
                        Verify that performance optimizations have not introduced new issues or regressions.
                    DB and Infra level bottleneck check
              
            * Pre Deployment:
                Encourage more logs for some days after deployment
                    Can maintian two levels of prod-log, just_deployed and deployed
                Deployment Plan, Stakeholder Communication
                Environment Setup: dev, stages, config management
                Testing
                Backup
                Deployment Readiness:
                    verify checklist
                        all pre-deployment check
                        rollback readiness check
                    create necessary training and documentation
            * Deployment Methodologies:
                & Blue-Green Deployment:
                    Maintains two prod environments(blue and green)
                        New changes gets deployed on Green and migrate traffic to green once it's verified
                        If issues arise, switch back to blue
                        Caution:
                            Ensure Environment Parity
                            Backup Data
                            Post Switch
                & Canary Deployment:
                    Deploy to Canary Group, a small group of users or servers.
                        Monitor Performance -> Rollback
                    ~ For Pilot Testing
                    Caution:
                        Rollback criteria
                & Rolling Deployment:
                    Keep extending deployment to more group of servers
                & Feature toggle Deployment:
                    Feature flags allows to enable or disable features at runtime without redeploying code.
                    Need central managment like Zookeeper

            * Rollback Plan:
                ^ Always consider a roll-forward before rolling back
                & Versioned Releases
                & Strategy:
                    10-minute recovery rollback strategy
                & Backup:
                    Of current production data and images
                        Database:
                            Run rollback scripts
                                No schema changes = Safe to rollback.
                                Existing column added to a select stored procedure or view = Safe to rollback.
                                New nullable columns added = Developers must review before rolling back.
                                Significant schema changes = Unsafe to rollback.
                            Restore a database backup
                        Images:
                            Redeploy once data is corrected
                & Strategy:
                    Rollback
                        10-minute recovery rollback strategy
                        3-minute recovery rollback strategy
                        Immediate rollback strategy
                & Procedure:
                    Pre-defined procedure setup and if possible automate it
                & Automated Rollback:
                    Configuring your deployment pipeline to automatically revert to a previous stable version.
                        Monitoring and Detection -> Trigger Mechanism -> Rollback Execution
                    Regularly test rollback procedures, ensure they work
            
            * Post Deployment:
                Validation:
                    Sanity/Smoke Testing:
                        Most critical things are working or not
                        System Functioning check  
                Monitor:
                    Performance Monitoring
                    Error tracking 
                    User feedback
                Notify Stakeholders:
                Rollback if necessary:
                Documentation and Review:
                Performance Tuning:
                    Analyze load, CPU, memory usage. Increase the juice if required

                
        # Phases of an Prod incident:
            Incident discovery (system alert, user complaints, self-identification)
            Data collection (find what doesn’t work, who is affected, steps to reproduce, etc.)
            Criticality assessment (which side of the business is affected, types of users, calculate possible aftermath)
            Actionable steps to recover the system (fix, rollback, switching traffic, etc.)
            Minimization of damage done to users (run a script to restore user data, contact users if needed)
            Root cause analysis & discussion
            Postmortem sum up
            Work on action items to improve the system
        # Types of Testing
            Pilot testing:
                Roll out new features to a subset of a user only
            A/B testing:
                Maintains two version, it's to Determine which one performs better
                    // two different checkout
            Functional Testing:
                it performs its functions correctly as per requirements
            Non-Functional Testing:
                Test load, performance, Reliability
            Performance Testing
            Post deployment testing
            Regression Testing:
                Checks after bug fix, whether fix has affected other features as well
            Load Testing
            Stress Testing
            Usability Testing
            Security Testing
            Compatibility Testing
            Integration Testing
            System Testing

    ` END
*/
