/*

You should hire me because I excel at solving complex problems and always eager to learn more. I thrive in team settings and am committed to delivering high-quality work punctually. My love for technology keeps me motivated to continuously improve, bringing fresh ideas and approaches to the table. Plus, I have 3 years of experience with technologies like NodeJS, which align well with the job requirements.

My expectations include opportunities for growth and learning, impactful work, and a collaborative culture with clear communication and feedback. 


    In my previous organisation, we were using AWS lambda to create our APIs. I was new to backend development. One of my senior colleague of my division had just left the organisation and one product was about to go live. So every body was in rush.
    I had just implemented the new communication messaging system to reduce the Lambda calls per message and tested it in my dev environment by testing all the corner scenarios. But then I didn't know about AWS lambda time-out and when my code got to production, my queue consumer started timing out. And more thing about queue consumer is, if it doesn’t get the success response from the lambda then it'll retry few more times (which was configured 3 at that time). Then it moved to DLQ. That caused many users to get same message three times and rest of them none.
    Once I found out the issue, while monitoring after the deployment, I informed my senior developer about it. He understood the issue immediately and asked me what should we do? Fortunately for me, only I was pushing messages using that system as of now so we could afford to close the queue. So I decided to close the queue and avoid sending messages to any user on that day. So for some period of time we erased all the data from the queue and deactivated it.
    Then I took action, and break all the data into chunks so that it can process within lambda timeout and deployed it next day. It started working fine.
    And after discussion, we decided to explore and implement fargate for this and I was exploring it for couple of days, but since it was working fine after my bug fix, I too was moved in the same task which was about to go live and fargate implementation got in the backlog. 


*/
/*
    Couple of months back, I was working on a task to automate the calculation of the shelf life. So I had to make similar small changes at many places along with a major change at one place to handle it in UI and govern those similar small changes. I had tested all the flows, on my local system but forgot to keep the test result with me. But when I wanted to merge the PR to deploy on later stages (CDT and Stage). And it was a major requirement which testing in CE env got shifted couple of days back(from by wednesday to by monday) so I had to complete all the changes and testing before the weekends. So I was working on the weekend to complete the final touch on the tasks and testing, after completing that one of my senior, whose changes I had worked on wouldnt approve the PR because I didnt have any testing proof with me. His point was that I have made changes in his code so I need to provide the validation proof.But My point was, I have done the testing throughly and it'll require setting up a lot of data which will take hours to complete and it was already very late on saturday. I was understanding that he is right but there wasn't much fault from my side too as the deployment date got shifted and I was in rush so I forgot to collect the result data. That lead to a conflict between us, but later I completed and capture the changes after doing the testing. Once I showed it to him on sunday, he approved it then CE team later started testing. My learnings from this scenario was, even in the rush scenarios dont forget your basics and dont agree with the rushed tasks specially if it's being asked to deliver early just a couple of day back of completion.
*/
/*
A couple of months ago, I was working on a project to automate the calculation of shelf life. It was a complex task that required making several small changes and a significant update to the user interface. Due to some reasons, the testing schedule had been moved up, meaning I had to finish everything before the weekend. So, I thoroughly tested all the flows on my local system on Saturday. But, in my hurry, I made a rookie mistake—I forgot to save the test results.

When it was time to merge the PR for deployment into CDT and Stage, I ran into an issue because, when I asked for approval of the PR, one of my senior colleagues, whose code I had modified, wouldn’t approve my work without seeing the test results. He asked for validation proof, which I didn’t have since I was in a rush and forgot to collect the test data.

While I understood his concern—especially since I had altered his code—I felt the situation was challenging due to the tight deadline and the unexpected shift in the testing schedule. This led to a bit of a conflict between us. I was frustrated—I knew I had tested everything thoroughly, but gathering the data again and doing the testing would take hours. And it's only going in CDT and stage environment. Nevertheless, I decided to spend more time re-testing and capturing the necessary data. I showed him the results on Sunday, and he approved the PR. The CE team was then able to start their testing.

This experience taught me an important lesson: Even in rushed scenarios, don’t forget your basics, and don’t agree to rushed tasks, especially if they’re being asked to be delivered early just a couple of days before completion when I dont have enough time entertain all the remaining works.

*/
/*
    Production Incidents in a Company with No-blame Culture
*/
/*

    Appointment scheduling:
        Decided to use store procedure
        This API wasn't directly accessed from anywhere, once refreshed, it'll have to put some data.
        Before using that, we made sure that, DB load increase much on refresh

        Then there were various join operations already

*/