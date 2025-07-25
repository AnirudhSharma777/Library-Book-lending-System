export class UserRegister{
  name:string;
  email:string;
  password:string;
  role:string;

  constructor(){
    this.name = '';
    this.email = '';
    this.password = '';
    this.role = 'Member';
  }
}

export class UserLogin{
  email:string;
  password:string;

  constructor(){
    this.email = '';
    this.password = '';
  }
}
