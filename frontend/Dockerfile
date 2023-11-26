FROM node:21

WORKDIR /app

COPY . .

RUN npm install

RUN npm run build

ENV BACKEND_API_URL=http://backend:1234
EXPOSE 4173

CMD ["npm", "run", "preview"]

