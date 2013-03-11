console.info "we have coffee!"
window.myModule = angular.module "myModule", ["ngResource"]
window.myController = ($scope, $resource) ->
  ToDo = $resource('/api/todos/:id', id: '@id')

  $scope.todos = ToDo.query()
  $scope.addTodo = ->
    toAdd = new ToDo({text: $scope.todoText, done: false})
    toAdd.$save()
    $scope.todos.push toAdd
    $scope.todoText = null

  $scope.updateTodo = (todo) -> todo.$save()
  $scope.removeTodo = (todo) ->
    index = $scope.todos.indexOf(todo)
    todo.$remove()
    $scope.todos.splice(index, 1)