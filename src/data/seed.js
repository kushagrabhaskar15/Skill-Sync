export const initialUser = {
  id: 'self',
  name: 'Kushagra Bhaskar',
  email: 'kushagra@example.com',
  role: 'Computer Science Student',
  school: 'MAIT',
  bio: 'Interested in software engineering, AI and collaborative learning.',
  teaches: ['Java', 'Spring Boot', 'MySQL', 'React', 'Git'],
  learns: ['Machine Learning', 'Python', 'NLP', 'System Design'],
  interests: ['Open-source', 'AI', 'Problem solving'],
  goal: 'Become a Machine Learning Engineer',
};

export const initialPeers = [
  { id: 'alex', name: 'Alex Morgan', role: 'Computer Science Student', school: 'University of Toronto', bio: 'Interested in backend development and AI. I love building small projects with other learners.', teaches: ['Java', 'Spring Boot', 'MySQL'], learns: ['Python', 'Machine Learning'], interests: ['AI', 'Open-source'], availability: 'Evenings', level: 'Intermediate', status: 'suggested', color: 'violet' },
  { id: 'samira', name: 'Samira Khan', role: 'Data Science Student', school: 'University of Waterloo', bio: 'Data science student exploring the intersection of statistics and thoughtful product design.', teaches: ['Python', 'Statistics', 'Pandas'], learns: ['SQL', 'React'], interests: ['AI', 'Problem solving'], availability: 'Weekends', level: 'Intermediate', status: 'suggested', color: 'orange' },
  { id: 'rohan', name: 'Rohan Mehta', role: 'Software Developer', school: 'Independent learner', bio: 'Frontend engineer who enjoys explaining React patterns and learning cloud architecture.', teaches: ['React', 'Node.js', 'TypeScript'], learns: ['Java', 'Cloud'], interests: ['Open-source', 'Building products'], availability: 'Weekday mornings', level: 'Advanced', status: 'pending', color: 'mint' },
  { id: 'maya', name: 'Maya Chen', role: 'Machine Learning Engineer', school: 'University of British Columbia', bio: 'ML engineer and patient mentor. Currently curious about systems design and developer tools.', teaches: ['Machine Learning', 'Python', 'NLP'], learns: ['System Design', 'Rust'], interests: ['AI', 'Research'], availability: 'Evenings', level: 'Advanced', status: 'suggested', color: 'pink' },
  { id: 'noah', name: 'Noah Williams', role: 'Product Designer', school: 'OCAD University', bio: 'Designing inclusive digital products. Looking to pair with engineers on creative side projects.', teaches: ['Figma', 'Design systems', 'UX research'], learns: ['React', 'Accessibility'], interests: ['Design', 'Open-source'], availability: 'Weekends', level: 'Intermediate', status: 'connected', color: 'blue' },
];

export const initialMessages = {
  alex: [
    { id: 'a1', from: 'alex', text: 'Hey! I can help you with Spring Boot.', time: '10:42 AM' },
    { id: 'a2', from: 'self', text: "Thanks! I'd love to learn through a small project.", time: '10:44 AM' },
    { id: 'a3', from: 'alex', text: 'We can start with REST APIs and JPA.', time: '10:46 AM' },
  ],
  noah: [
    { id: 'n1', from: 'noah', text: 'Would you like to pair on a small accessible UI project this weekend?', time: '9:18 AM' },
  ],
  maya: [{ id: 'm1', from: 'maya', text: 'I saw that you are learning ML. Happy to share a few resources!', time: 'Yesterday' }],
};

export const initialActivities = [
  'You completed your profile.',
  'New peer recommendations are available.',
  'Alex Morgan accepted your connection request.',
];
